package fr.insee.arc.core.service.p4controle.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.dao.DatabaseConnexionConfiguration;
import fr.insee.arc.core.service.global.dao.GenericQueryDao;
import fr.insee.arc.core.service.global.dao.HashFileNameConversion;
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
import fr.insee.arc.core.service.p4controle.bo.ControleMarkCode;
import fr.insee.arc.core.service.p4controle.dao.ThreadControleQueryBuilder;
import fr.insee.arc.core.service.p4controle.operation.ControleRulesOperation;
import fr.insee.arc.core.service.p4controle.operation.ServiceJeuDeRegleOperation;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.Sleep;

/**
 * Comme pour le normage, on parallélise en controlant chaque
 * fichier dans des threads séparés.
 * 
 * @author S4LWO8
 *
 */
public class ThreadControleService extends ThreadTemplate {

	private static final Logger LOGGER = LogManager.getLogger(ThreadControleService.class);
	
	private String idSource;
	
	private String tableControleDataTemp;
	private String tableControlePilTemp;
	private String tableOutOkTemp = "tableOutOkTemp";
	private String tableOutKoTemp = "tableOutKoTemp";
	private String tableOutOk;
	private String tableOutKo;

	private ServiceJeuDeRegleOperation sjdr;
		
    private ThreadOperations arcThreadGenericDao;
	private GenericQueryDao genericExecutorDao;
	
	private TraitementPhase currentExecutedPhase = TraitementPhase.CONTROLE;
	private TraitementPhase previousExecutedPhase = this.currentExecutedPhase.previousPhase();
	private String previousExecutedPhaseTable;

	public void configThread(ScalableConnection connexion, int currentIndice, ThreadMultiphaseService theApi, boolean beginNextPhase, boolean cleanPhase) {

		this.envExecution = theApi.getEnvExecution();
		this.idSource = theApi.getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(currentIndice);
		this.connexion = connexion;
		this.tablePil = theApi.getTablePil();
		this.tablePilTemp = theApi.getTablePilTemp();
		this.tabIdSource=theApi.getTabIdSource();
		this.paramBatch=theApi.getParamBatch();

		this.sjdr = new ServiceJeuDeRegleOperation();

		// Nom des tables temporaires
		this.tableControleDataTemp = ThreadTemporaryTable.TABLE_CONTROLE_DATA_TEMP;
		this.tableControlePilTemp = ThreadTemporaryTable.TABLE_PILOTAGE_THREAD;

		// tables finales
		this.tableOutOk = TableNaming.phaseDataTableName(theApi.getEnvExecution(), this.currentExecutedPhase, TraitementEtat.OK);
		this.tableOutKo = TableNaming.phaseDataTableName(theApi.getEnvExecution(), this.currentExecutedPhase, TraitementEtat.KO);
		
		this.previousExecutedPhaseTable = TableNaming.phaseDataTableName(this.envExecution, this.previousExecutedPhase, TraitementEtat.OK);
		
		// arc thread dao
		arcThreadGenericDao=new ThreadOperations(this.currentExecutedPhase, beginNextPhase, cleanPhase, connexion, tablePil, tablePilTemp, tableControlePilTemp, previousExecutedPhaseTable, paramBatch, idSource);
    	genericExecutorDao = new GenericQueryDao(this.connexion.getExecutorConnection());

	}
	
	
	public void run() {
		try {

			preparation();

			execute();

			insertionFinale();

		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, "Error in control Thread");
			try {
				PilotageOperations.traitementSurErreur(this.connexion.getCoordinatorConnection(), this.currentExecutedPhase, this.tablePil,
						this.idSource, e);
			} catch (ArcException e2) {
				StaticLoggerDispatcher.error(LOGGER, e2);
			}
			Sleep.sleep(ThreadConstant.PREVENT_ERROR_SPAM_DELAY);
		}
	}

	/**
	 * Préparation des données et implémentation des jeux de règles utiles
	 *
	 * @param this.connexion
	 *
	 * @param tableIn         la table issue du chargement-normage
	 *
	 * @param env             l'environnement d'execution
	 * @param tableControle   la table temporaire à controler
	 * @param tablePilTemp    la table temporaire listant les fichiers en cours de
	 *                        traitement
	 * @param tableJeuDeRegle la table des jeux de règles
	 * @param tableRegleC     la table des règles de controles
	 * @throws ArcException
	 */
	private void preparation() throws ArcException {

		genericExecutorDao.initialize();
		
		StaticLoggerDispatcher.info(LOGGER, "** preparation **");
		genericExecutorDao.addOperation(arcThreadGenericDao.preparationDefaultDao());
		
		// Marquage du jeux de règles appliqué
		StaticLoggerDispatcher.info(LOGGER, "Marquage du jeux de règles appliqués ");
		genericExecutorDao.addOperation(RulesOperations.marqueJeuDeRegleApplique(this.currentExecutedPhase, this.envExecution, this.tableControlePilTemp, TraitementEtat.OK.toString()));

		// Fabrication de la table de controle temporaire
		StaticLoggerDispatcher.info(LOGGER, "Fabrication de la table de controle temporaire ");
		genericExecutorDao.addOperation(TableOperations.createTableTravailIdSource(this.previousExecutedPhaseTable, this.tableControleDataTemp, this.idSource,
				ThreadControleQueryBuilder.extraColumnsAddedByControle()));

		genericExecutorDao.execute();

	}

	/**
	 * Méthode pour controler une table
	 *
	 * @param connexion
	 *
	 * @param tableControle la table à controler
	 *
	 * @throws ArcException
	 */
	private void execute() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "** execute CONTROLE sur la table : " + this.tableControleDataTemp + " **");
		
		FileIdCard fileIdCard = RulesOperations.fileIdCardFromPilotage(this.connexion.getExecutorConnection(),
				tableControlePilTemp, this.idSource);
		
		// Récupération des Jeux de règles associés
		ControleRulesOperation.fillControleRules(this.connexion.getExecutorConnection(), this.envExecution, fileIdCard);
		this.sjdr.executeJeuDeRegle(this.connexion.getExecutorConnection(), fileIdCard, this.tableControleDataTemp);

	}

	/**
	 * Méthode à passer après les controles
	 *
	 * @param connexion
	 *
	 * @param tableIn    la table temporaire avec les marquage du controle
	 * @param tableOutOk la table permanente sur laquelle on ajoute les bons
	 *                   enregistrements de tableIn
	 * @param tableOutKo la table permanente sur laquelle on ajoute les mauvais
	 *                   enregistrements de tableIn
	 * @param tablePil   la table de pilotage des fichiers
	 * @param tableSeuil la table des seuils
	 * @throws ArcException
	 */
	private StringBuilder calculSeuilControle() {
		StaticLoggerDispatcher.info(LOGGER, "finControle");

		StringBuilder blocFin = new StringBuilder();
		// Creation des tables temporaires ok et ko
		StaticLoggerDispatcher.info(LOGGER, "Creation des tables temporaires ok et ko");

		// Execution à mi parcours du bloc de requete afin que les tables tempo soit
		// bien créées
		// ensuite dans le java on s'appuie sur le dessin de ces tables pour ecrire du
		// SQL
		blocFin.append(TableOperations.creationTableResultat(this.tableControleDataTemp, tableOutOkTemp));
		blocFin.append(TableOperations.creationTableResultat(this.tableControleDataTemp, tableOutKoTemp));

		// Marquage des résultat du control dans la table de pilotage
		StaticLoggerDispatcher.info(LOGGER, "Marquage dans la table de pilotage");
		blocFin.append(marquagePilotage());

		StaticLoggerDispatcher.info(LOGGER, "Insertion dans OK");
		blocFin.append(ThreadControleQueryBuilder.querySelectRecordsOK(tableControleDataTemp, tableOutOkTemp, tableControlePilTemp));

		// insert in OK when
		// etat traitement in KO
		// OR records which have errors that must be excluded
		StaticLoggerDispatcher.info(LOGGER, "Insertion dans KO");
		blocFin.append(ThreadControleQueryBuilder.querySelectRecordsKO(tableControleDataTemp, tableOutKoTemp, tableControlePilTemp));

		return blocFin;
	}

	/**
	 * Insertion dans les tables finales
	 * 
	 * @throws ArcException
	 */
	private void insertionFinale() throws ArcException {

		// calcul des seuils pour finalisation
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(calculSeuilControle());

		// promote the application user account to full right
		query.append(DatabaseConnexionConfiguration.switchToFullRightRole());

		// Créer les tables héritées
		String tableIdSourceOK = HashFileNameConversion.tableOfIdSource(tableOutOk, this.idSource);
		query.append(TableOperations.createTableInherit(tableOutOkTemp, tableIdSourceOK));
		String tableIdSourceKO = HashFileNameConversion.tableOfIdSource(tableOutKo, this.idSource);
		query.append(TableOperations.createTableInherit(tableOutKoTemp, tableIdSourceKO));

		query.append(FormatSQL.dropTable(ThreadTemporaryTable.TABLE_CONTROLE_DATA_TEMP
				, ThreadTemporaryTable.TABLE_CONTROLE_MARK_TEMP
				, ThreadTemporaryTable.TABLE_CONTROLE_META_TEMP
				, ThreadTemporaryTable.TABLE_CONTROLE_ROW_TOTAL_COUNT_TEMP));
		
		// mark file as done in the pilotage table
		arcThreadGenericDao.marquageFinalDefaultDao(query);
		
	}

	/**
	 * Marque les résultats des contrôles dans la table de pilotage
	 * 
	 * @return
	 */
	private String marquagePilotage() {
		
		
		
		StringBuilder blocFin = new StringBuilder();
		blocFin.append("\n UPDATE " + this.tableControlePilTemp + " ");
		blocFin.append("\n SET etat_traitement= ");
		blocFin.append("\n case ");
		blocFin.append("\n when exists (select from " + ThreadTemporaryTable.TABLE_CONTROLE_META_TEMP
				+ " where blocking) then '{" + TraitementEtat.KO + "}'::text[] ");
		blocFin.append("\n when exists (select from " + ThreadTemporaryTable.TABLE_CONTROLE_META_TEMP + " where controle='"
				+ ControleMarkCode.RECORD_WITH_ERROR_TO_EXCLUDE.getCode() + "') then '{" + TraitementEtat.OK + ","
				+ TraitementEtat.KO + "}'::text[] ");
		blocFin.append("\n else '{OK}'::text[] ");
		blocFin.append("\n end ");
		blocFin.append(
				"\n , rapport='Control failed on : '||(select array_agg(brokenrules||case when blocking then ' (blocking rules)' else '' end||case when controle='"
						+ ControleMarkCode.RECORD_WITH_ERROR_TO_EXCLUDE.getCode()
						+ "' then ' (exclusion rules)' else '' end)::text from "
						+ ThreadTemporaryTable.TABLE_CONTROLE_META_TEMP + ") ");
		blocFin.append("\n WHERE exists (select from " + ThreadTemporaryTable.TABLE_CONTROLE_META_TEMP + ") ");
		blocFin.append(";");
		return blocFin.toString();
	}


	// Getter et Setter
	public ServiceJeuDeRegleOperation getSjdr() {
		return this.sjdr;
	}

	public void setSjdr(ServiceJeuDeRegleOperation sjdr) {
		this.sjdr = sjdr;
	}

}
