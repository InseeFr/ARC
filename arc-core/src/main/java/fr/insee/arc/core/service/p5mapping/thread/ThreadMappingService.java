package fr.insee.arc.core.service.p5mapping.thread;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegleDao;
import fr.insee.arc.core.service.global.dao.DatabaseConnexionConfiguration;
import fr.insee.arc.core.service.global.dao.GenericQueryDao;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.core.service.global.dao.RulesOperations;
import fr.insee.arc.core.service.global.dao.TableOperations;
import fr.insee.arc.core.service.global.dao.ThreadOperations;
import fr.insee.arc.core.service.global.scalability.ScalableConnection;
import fr.insee.arc.core.service.global.thread.IThread;
import fr.insee.arc.core.service.p5mapping.ApiMappingService;
import fr.insee.arc.core.service.p5mapping.dao.MappingQueries;
import fr.insee.arc.core.service.p5mapping.dao.ThreadMappingQueries;
import fr.insee.arc.core.service.p5mapping.operation.MappingOperation;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.Sleep;

/**
 * @author S4LWO8
 *
 */
public class ThreadMappingService extends ApiMappingService implements Runnable, IThread<ApiMappingService> {

	private static final Logger LOGGER = LogManager.getLogger(ThreadMappingService.class);

	private Thread t;

	private int indice;
	private String tableTempControleOk;
	private String tableMappingPilTemp;

	private ThreadOperations arcThreadGenericDao;

	private GenericQueryDao genericExecutorDao;

	@Override
	public void configThread(ScalableConnection connexion, int currentIndice, ApiMappingService anApi) {

		this.connexion = connexion;
		this.indice = currentIndice;
		this.idSource = anApi.getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(indice);
		this.envExecution = anApi.getEnvExecution();
		this.tablePilTemp = anApi.getTablePilTemp();
		this.currentPhase = anApi.getCurrentPhase();
		this.tablePrevious = anApi.getTablePrevious();
		this.tabIdSource = anApi.getTabIdSource();
		this.paramBatch = anApi.getParamBatch();

		this.tableTempControleOk = "tableTempControleOk".toLowerCase();
		this.tableMappingPilTemp = "tableMappingPilTemp".toLowerCase();

		this.tablePil = anApi.getTablePil();

		// thread generic dao
		arcThreadGenericDao = new ThreadOperations(connexion, tablePil, tablePilTemp, tableMappingPilTemp,
				tablePrevious, paramBatch, idSource);
		genericExecutorDao = new GenericQueryDao(this.connexion.getExecutorConnection());

	}

	public void start() {
		StaticLoggerDispatcher.debug(LOGGER, "Starting ThreadMappingService");
		t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		try {
			this.preparerExecution();

			execute();

		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, e);

			try {
				PilotageOperations.traitementSurErreur(this.connexion.getCoordinatorConnection(),
						this.getCurrentPhase(), this.tablePil, this.idSource, e);
			} catch (ArcException e2) {
				StaticLoggerDispatcher.error(LOGGER, e);

			}
			Sleep.sleep(PREVENT_ERROR_SPAM_DELAY);
		}
	}

	/**
	 * @throws ArcException
	 */
	private void preparerExecution() throws ArcException {
		genericExecutorDao.initialize();
		genericExecutorDao.addOperation(this.arcThreadGenericDao.preparationDefaultDao());
		genericExecutorDao.addOperation(RulesOperations.marqueJeuDeRegleApplique(this.getCurrentPhase(),
				this.envExecution, this.tableMappingPilTemp));
		genericExecutorDao.addOperation(TableOperations.createTableTravailIdSource(this.getTablePrevious(),
				this.tableTempControleOk, this.idSource));
		genericExecutorDao.executeAsTransaction();
	}

	private void execute() throws ArcException {

		JeuDeRegle jdr = getTheRulesSetOfTheFile();

		MappingOperation serviceMapping = new MappingOperation();
		this.regleMappingFactory = serviceMapping.construireRegleMappingFactory(this.connexion.getExecutorConnection(),
				this.getEnvExecution(), this.tableTempControleOk, getPrefixidentifiantrubrique());

		/*
		 * Récupération de l'id_famille
		 */
		String idFamille = ThreadMappingQueries.fetchIdFamille(this.connexion.getExecutorConnection(), jdr,
				this.getEnvExecution());
		/*
		 * Instancier une requête de mapping générique pour ce jeu de règles.
		 */
		MappingQueries requeteMapping = new MappingQueries(this.connexion.getExecutorConnection(),
				this.regleMappingFactory, idFamille, jdr, this.getEnvExecution(), this.tableTempControleOk,
				this.indice);
		/*
		 * Construire la requête de mapping (dérivation des règles)
		 */
		requeteMapping.construire();

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		// Créer les tables temporaires métier
		query.append(requeteMapping.requeteCreationTablesTemporaires());
		// calculer la requete du fichier
		query.append(requeteMapping.getRequete(idSource));

		/*
		 * Transfert des tables métier temporaires vers les tables définitives
		 */
		query.append(requeteMapping.requeteTransfertVersTablesMetierDefinitives());

		query.append(PilotageOperations.queryUpdatePilotageMapping(this.tableMappingPilTemp, this.idSource));

		// promote the application user account to full right
		query.append(DatabaseConnexionConfiguration.switchToFullRightRole());

		/*
		 * Transfert de la table mapping_ko temporaire vers la table mapping_ko
		 * définitive
		 */

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

	@Override
	public ScalableConnection getConnexion() {
		return connexion;
	}

	@Override
	public Thread getT() {
		return t;
	}
}
