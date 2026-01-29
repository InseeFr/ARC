package fr.insee.arc.core.service.p5mapping.thread;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegleDao;
import fr.insee.arc.core.service.global.dao.DatabaseConnexionConfiguration;
import fr.insee.arc.core.service.global.dao.GenericQueryDao;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.core.service.global.dao.RulesOperations;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.core.service.global.dao.TableOperations;
import fr.insee.arc.core.service.global.dao.ThreadOperations;
import fr.insee.arc.core.service.global.scalability.ScalableConnection;
import fr.insee.arc.core.service.global.thread.ThreadConstant;
import fr.insee.arc.core.service.global.thread.ThreadTemplate;
import fr.insee.arc.core.service.global.thread.ThreadTemporaryTable;
import fr.insee.arc.core.service.mutiphase.thread.ThreadMultiphaseService;
import fr.insee.arc.core.service.p5mapping.bo.TableMapping;
import fr.insee.arc.core.service.p5mapping.dao.MappingQueries;
import fr.insee.arc.core.service.p5mapping.dao.MappingQueriesFactory;
import fr.insee.arc.core.service.p5mapping.dao.ThreadMappingQueries;
import fr.insee.arc.core.service.p5mapping.operation.MappingOperation;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.Sleep;

/**
 * @author S4LWO8
 *
 */
public class ThreadMappingService extends ThreadTemplate {

	private static final Logger LOGGER = LogManager.getLogger(ThreadMappingService.class);
	
    private static final String PREFIX_IDENTIFIANT_RUBRIQUE = "i_";
	
	private String idSource;

	private int indice;
	private String tableTempControleOk;
	private String tableMappingPilTemp;
	private String tableLienIdentifiants;
	
	private ThreadOperations arcThreadGenericDao;

	private GenericQueryDao genericExecutorDao;

	private Map<TableMapping, StringBuilder> queriesThatInsertDataIntoTemporaryModelTables = new HashMap<>();
	
	
	private TraitementPhase currentExecutedPhase = TraitementPhase.MAPPING;
	private TraitementPhase previousExecutedPhase = this.currentExecutedPhase.previousPhase();
	private String previousExecutedPhaseTable;
	
	public void configThread(ScalableConnection connexion, int currentIndice, ThreadMultiphaseService anApi, boolean beginNextPhase, boolean cleanPhase) {

		this.connexion = connexion;
		this.indice = currentIndice;
		this.idSource = anApi.getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(indice);
		this.envExecution = anApi.getEnvExecution();
		this.tablePilTemp = anApi.getTablePilTemp();
		this.tabIdSource = anApi.getTabIdSource();
		this.paramBatch = anApi.getParamBatch();

		this.tableTempControleOk = ViewEnum.getFullName(envExecution, FormatSQL.temporaryTableName(ThreadTemporaryTable.TABLE_MAPPING_DATA_TEMP));
		this.tableMappingPilTemp = ThreadTemporaryTable.TABLE_PILOTAGE_THREAD;

		this.tableLienIdentifiants = ViewEnum.getFullName(envExecution, FormatSQL.temporaryTableName(ThreadTemporaryTable.TABLE_MAPPING_IDS_LINK_TEMP));
		
		this.tablePil = anApi.getTablePil();
		this.genericExecutorDao = new GenericQueryDao(this.connexion.getExecutorConnection());

		this.previousExecutedPhaseTable = TableNaming.phaseDataTableName(this.envExecution, this.previousExecutedPhase, TraitementEtat.OK);
		
		// thread generic dao
		this.arcThreadGenericDao = new ThreadOperations(this.currentExecutedPhase, beginNextPhase, cleanPhase, connexion, tablePil, tablePilTemp, tableMappingPilTemp,
				this.previousExecutedPhaseTable, paramBatch, idSource);
	}

	public void run() {
		try {
			this.preparerExecution();

			execute();

		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, e);

			try {
				
				// drop temporary tables
				genericExecutorDao.initialize().addOperation(FormatSQL.dropTable(retrieveMappingTemporaryTables())).execute();
				
				PilotageOperations.traitementSurErreur(this.connexion.getCoordinatorConnection(),
						this.currentExecutedPhase, this.tablePil, this.idSource, e);

			} catch (ArcException e2) {
				StaticLoggerDispatcher.error(LOGGER, e);

			}
			Sleep.sleep(ThreadConstant.PREVENT_ERROR_SPAM_DELAY);
		}
	}

	/**
	 * Returns the list of temporary tables generated thrue the mapping process
	 * @return
	 */
	private String[] retrieveMappingTemporaryTables() {
		Set<String> temporaryTablesToRemove = new HashSet<>();
		temporaryTablesToRemove.add(this.tableTempControleOk);
		temporaryTablesToRemove.add(this.tableLienIdentifiants);
		this.queriesThatInsertDataIntoTemporaryModelTables.keySet().stream().forEach(t-> temporaryTablesToRemove.add(t.getNomTableTemporaire()));
		return temporaryTablesToRemove.toArray(new String[0]);
	}

	/**
	 * @throws ArcException
	 */
	private void preparerExecution() throws ArcException {
		genericExecutorDao.initialize()
			.addOperation(this.arcThreadGenericDao.preparationDefaultDao())
			.addOperation(RulesOperations.marqueJeuDeRegleApplique(this.currentExecutedPhase, 
				this.envExecution, this.tableMappingPilTemp))
			.addOperation(TableOperations.createTableTravailIdSource(this.previousExecutedPhaseTable,
				this.tableTempControleOk, this.idSource))
			.executeAsTransaction();
	}

	private void execute() throws ArcException {

		JeuDeRegle jdr = getTheRulesSetOfTheFile();

		MappingOperation serviceMapping = new MappingOperation();
	    MappingQueriesFactory regleMappingFactory = serviceMapping.construireRegleMappingFactory(this.connexion.getExecutorConnection(),
				this.getEnvExecution(), this.tableTempControleOk, PREFIX_IDENTIFIANT_RUBRIQUE);

		/*
		 * Récupération de l'id_famille
		 */
		String idFamille = ThreadMappingQueries.fetchIdFamille(this.connexion.getExecutorConnection(), jdr,
				this.getEnvExecution());
		/*
		 * Instancier une requête de mapping générique pour ce jeu de règles.
		 */
		MappingQueries requeteMapping = new MappingQueries(this.connexion.getExecutorConnection(),
				regleMappingFactory, idFamille, jdr, this.getEnvExecution(), this.tableTempControleOk,
				this.indice);

		/*
		 * Build and execute the query that computes and links all identifiers between each others
		 */
		requeteMapping.setTableLienIdentifiants(this.tableLienIdentifiants);
		requeteMapping.construire();
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		query.append(requeteMapping.construireTableLienIdentifiants());
		
		/*
		 * For each model table, compute a query that inserts the data into a temporary model table.
		 */
		this.queriesThatInsertDataIntoTemporaryModelTables = requeteMapping.getQueriesThatInsertDataIntoTemporaryModelTable(idSource);
		
		/*
		 * Execute the data insertion into the temporary model tables through parallel threads
		 */
		queriesThatInsertDataIntoTemporaryModelTables.values().stream().forEach(tableQuery -> query.append(tableQuery));
		
		/*
		 * Delete empty records if there is not link in children tables
		 */
		
		query.append(requeteMapping.deleteEmptyRecords(idSource));

		// clean the input temporary data table
		query.append(FormatSQL.dropTable(this.tableTempControleOk));

		// promote the application user account to full right
		query.append(DatabaseConnexionConfiguration.switchToFullRightRole());
		
		/*
		 * Transfert des tables métier temporaires vers les tables définitives
		 */
		query.append(requeteMapping.requeteTransfertVersTablesMetierDefinitives());

		query.append(PilotageOperations.queryUpdatePilotageMapping(this.tableMappingPilTemp, this.idSource));

		arcThreadGenericDao.marquageFinalDefaultDao(query);

	}

	private JeuDeRegle getTheRulesSetOfTheFile() throws ArcException {

		/*
		 * Construire l'ensemble des jeux de règles
		 */
		List<JeuDeRegle> listeJeuxDeRegles = JeuDeRegleDao.recupJeuDeRegle(this.connexion.getExecutorConnection(),
				this.getEnvExecution(), this.tableTempControleOk);

		if (listeJeuxDeRegles.isEmpty()) {
			throw new ArcException(ArcExceptionMessage.MAPPING_RULES_NOT_FOUND);
		}

		if (listeJeuxDeRegles.size() > 1) {
			throw new ArcException(ArcExceptionMessage.MAPPING_RULES_NON_UNIQUE);
		}

		return listeJeuxDeRegles.get(0);
	}

}
