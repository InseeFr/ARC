package fr.insee.arc.core.service.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.ServiceReporting;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementTableExecution;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.service.api.query.ServiceDatabaseConfiguration;
import fr.insee.arc.core.service.api.query.ServicePilotageOperation;
import fr.insee.arc.core.service.api.query.ServiceTableNaming;
import fr.insee.arc.core.service.thread.ScalableConnection;
import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.core.util.Norme;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.ressourceUtils.SpringApplicationContext;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;

@Component
public abstract class ApiService implements IConstanteNumerique {

	protected static final Logger LOGGER_APISERVICE = LogManager.getLogger(ApiService.class);

	public static final String FICHIER_MISE_EN_PRODUCTION = "production.dummy";

	protected int maxParallelWorkers;


	// racine xml
	public static final String ROOT = "root";

	// anti-spam delay when thread chain error
	protected static final int PREVENT_ERROR_SPAM_DELAY = 100;

	@Autowired
	protected PropertiesHandler properties;

	@Autowired
	@Qualifier("activeLoggerDispatcher")
	protected LoggerDispatcher loggerDispatcher;

	protected ScalableConnection connexion;
	
	protected String envExecution;
	protected String envParameters;
	protected String tablePrevious;
	protected String previousPhase;
	protected String currentPhase;
	protected String tablePil;
	protected String tablePilTemp;
	protected String tableNorme;
	protected String tableJeuDeRegle;
	protected String tableChargementRegle;
	protected String tableNormageRegle;
	protected String tableMappingRegle;
	protected String tableControleRegle;
	protected Integer nbEnr;
	protected String tableCalendrier;
	protected String directoryRoot;
	protected String paramBatch = null;
	protected String currentIdSource;
    protected String directoryIn;
    protected List<Norme> listeNorme;
	
	// made to report the number of object processed by the phase
	private int reportNumberOfObject = 0;

	public static final String bdDateFormat = "DD/MM/YYYY HH24:MI:SS";

	public static final String IHM_SCHEMA = "arc.ihm";

	protected String idSource;

	protected Boolean todo = false;

	private HashMap<String, ArrayList<String>> tabIdSource;

	public ApiService() {
		super();
		springInit();
	}

	protected ApiService(String aCurrentPhase, String aParametersEnvironment, String aEnvExecution, String aDirectoryRoot,
			Integer aNbEnr, String paramBatch) {
		this();
		StaticLoggerDispatcher.info("** initialiserVariable **", LOGGER_APISERVICE);
		try {
			this.connexion = new ScalableConnection(UtilitaireDao.get(0).getDriverConnexion());
		} catch (Exception ex) {
			LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "Error in initializing connexion");
		}

		this.setParamBatch(paramBatch);

		// Initialisation de la phase
		this.setCurrentPhase(aCurrentPhase);
		this.setPreviousPhase(TraitementPhase.valueOf(this.getCurrentPhase()).previousPhase().toString());
		// Table en entrée
		this.setEnvExecution(aEnvExecution);
		this.envParameters = aParametersEnvironment;
		this.setDirectoryRoot(aDirectoryRoot);

		this.setTablePrevious((ServiceTableNaming.dbEnv(aEnvExecution) + this.getPreviousPhase() + "_" + TraitementEtat.OK).toLowerCase());

		// Tables de pilotage et pilotage temporaire
		this.setTablePil(ServiceTableNaming.dbEnv(aEnvExecution) + TraitementTableExecution.PILOTAGE_FICHIER);
		this.tablePilTemp = ServiceTableNaming.temporaryTableName(aEnvExecution, aCurrentPhase,
				TraitementTableExecution.PILOTAGE_FICHIER.toString(), "0");
		this.setTableNorme(ServiceTableNaming.dbEnv(aEnvExecution) + TraitementTableParametre.NORME);
		this.tableCalendrier = ServiceTableNaming.dbEnv(aEnvExecution) + TraitementTableParametre.CALENDRIER;
		// Tables venant de l'initialisation globale
		this.setTableJeuDeRegle(ServiceTableNaming.dbEnv(aEnvExecution) + TraitementTableParametre.JEUDEREGLE);
		this.setTableChargementRegle(ServiceTableNaming.dbEnv(aEnvExecution) + TraitementTableParametre.CHARGEMENT_REGLE);
		this.setTableNormageRegle(ServiceTableNaming.dbEnv(aEnvExecution) + TraitementTableParametre.NORMAGE_REGLE);
		this.setTableControleRegle(ServiceTableNaming.dbEnv(aEnvExecution) + TraitementTableParametre.CONTROLE_REGLE);
		this.setTableMappingRegle(ServiceTableNaming.dbEnv(aEnvExecution) + TraitementTableParametre.MAPPING_REGLE);
		this.setTableOutKo((ServiceTableNaming.dbEnv(aEnvExecution) + this.getCurrentPhase() + "_" + TraitementEtat.KO).toLowerCase());
		this.setNbEnr(aNbEnr);

		StaticLoggerDispatcher.info("** Fin constructeur ApiService **", LOGGER_APISERVICE);
	}

	/**
	 * Compteur simple pour tester la boucle d'execution
	 */
	private String tableOutKo;

	/**
	 * Initialisation des variable et des noms de table
	 *
	 * @param aEnvExecution
	 * @param aPreviousPhase
	 * @param aCurrentPhase
	 * @param aNbEnr
	 */
	private boolean initialiser() {
		loggerDispatcher.info("** initialiser **", LOGGER_APISERVICE);
		// Vérifie si y'a des sources à traiter
		if (this.todo) {
			try {
				UtilitaireDao.get(0).executeBlock(this.connexion.getCoordinatorConnection(), ServiceDatabaseConfiguration.configConnection(this.getEnvExecution()));
			} catch (ArcException ex) {
				LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "initialiser()", ex);
			}
			register(this.connexion.getCoordinatorConnection(), this.getPreviousPhase(), this.getCurrentPhase(), this.getTablePil(),
					this.tablePilTemp, this.getNbEnr());
		}

		return this.todo;
	}

	/** Manually autowire the factory-produced instance. */
	private void springInit() {
		try {
			SpringApplicationContext.autowire(this);
		} catch (Exception e) {
			//
		}
	}

	/**
	 * Vérifier si y'a des fichiers à traiter on teste dans la phase précédente si
	 * on trouve des fichiers OK avec etape=1
	 *
	 * @param tablePil
	 * @param phaseAncien
	 * @return
	 */
	private boolean checkTodo(String tablePil, String phaseAncien, String phaseNouveau) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		boolean checkTodoResult = false;
		requete.append("SELECT 1 FROM " + tablePil + " a ");
		requete.append("WHERE phase_traitement=" + requete.quoteText(phaseAncien) + " AND "
				+ requete.quoteText(TraitementEtat.OK.toString()) + "=ANY(etat_traitement) ");
		requete.append("and etape=1 ");
		requete.append("limit 1 ");
		try {
			checkTodoResult = UtilitaireDao.get(0).hasResults(this.connexion.getCoordinatorConnection(), requete);
		} catch (Exception ex) {
			LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "checkTodo()", ex);
		}
		return checkTodoResult;
	}

	/**
	 * Marque dans la table de pilotage les id_source qui vont être traités dans la
	 * phase Si des id_source sont déjà en traitement, la méthode en selectionnera
	 * de nouveaux Copie la table de pilotage pour les id_source selectionnés. Cette
	 * table sera mis à jour pendant l'éxécution de la phase.
	 * 
	 * @param connexion
	 * @param phaseIn
	 * @param phase
	 * @param tablePil
	 * @param tablePilTemp
	 * @param nbEnr
	 * @throws ArcException
	 */
	private void register(Connection connexion, String phaseIn, String phase, String tablePil, String tablePilTemp,
			Integer nbEnr) {
		loggerDispatcher.info("** register **", LOGGER_APISERVICE);
		try {
			UtilitaireDao.get(0).executeBlock(connexion,
					ServicePilotageOperation.copieTablePilotage(tablePil, tablePilTemp, phaseIn, phase, nbEnr));
		} catch (Exception ex) {
			LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "register()", ex);
		}
	}

	/**
	 * Méthode pour marquer la table de pilotage temporaire avec le jeu de règle
	 * appliqué
	 *
	 * @return
	 */
	protected String marqueJeuDeRegleApplique(String pilTemp) {
		return marqueJeuDeRegleApplique(pilTemp, null);
	}

	protected String marqueJeuDeRegleApplique(String pilTemp, String defaultEtatTraitement) {
		StringBuilder requete = new StringBuilder();
		requete.append("WITH ");
		requete.append(
				"prep AS (SELECT a."+ColumnEnum.ID_SOURCE.getColumnName()+", a.id_norme, a.periodicite, b.validite_inf, b.validite_sup, b.version ");
		requete.append("	FROM " + pilTemp + " a  ");
		requete.append("	INNER JOIN " + this.getTableJeuDeRegle()
				+ " b ON a.id_norme=b.id_norme AND a.periodicite=b.periodicite AND b.validite_inf <=a.validite::date AND b.validite_sup>=a.validite::date ");
		requete.append("	WHERE phase_traitement='" + this.getCurrentPhase() + "') ");
		requete.append("UPDATE " + pilTemp + " AS a ");
		requete.append("SET validite_inf=prep.validite_inf, validite_sup=prep.validite_sup, version=prep.version ");
		if (defaultEtatTraitement != null) {
			requete.append(", etat_traitement='{" + defaultEtatTraitement + "}'");
		}
		requete.append("FROM prep ");
		requete.append("WHERE a.phase_traitement='" + this.getCurrentPhase() + "'; ");
		return requete.toString();
	}

	/**
	 * promote the application to the full right user role if required. required is
	 * true if the restrictedUserAccount exists
	 * 
	 * @throws ArcException
	 */
	public String switchToFullRightRole() {
		if (!properties.getDatabaseRestrictedUsername().equals("")) {
			return FormatSQL.changeRole(properties.getDatabaseUsername());
		}
		return "";
	}


	public abstract void executer() throws ArcException;

	/**
	 * Finalise l'appel d'une phase Marque dans la table de pilotage globale les
	 * id_source qui ont été traités (recopie des état de la table de pilotage
	 * temporaire de la phase vers la table de pilotage globale) Efface les objets
	 * temporaires (tables, type, ...)
	 */
	public void finaliser() {
		loggerDispatcher.info("finaliser", LOGGER_APISERVICE);

		try {
			if (Boolean.TRUE.equals(this.todo)) {

				StringBuilder requete = new StringBuilder();
				requete.append(FormatSQL.dropTable(this.tablePilTemp));
				try {
					UtilitaireDao.get(0).executeBlock(this.connexion.getCoordinatorConnection(), requete);
				} catch (Exception ex) {
					LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "finaliser()", ex);
				}
			}
		} finally {
			try {
				if (this.connexion.getCoordinatorConnection() != null) {
					this.connexion.getCoordinatorConnection().close();
					this.connexion.setCoordinatorConnection(null);
				}
			} catch (Exception ex) {
				LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "finaliser()", ex);
			}

		}
	}

	/**
	 * liste les id_source pour une phase et un etat donnée dans une table de
	 * pilotage
	 *
	 * @param tablePilotage
	 * @param aCurrentPhase
	 * @param etat
	 * @return
	 */
	public HashMap<String, ArrayList<String>> pilotageListIdsource(String tablePilotage, String aCurrentPhase,
			String etat) {
		loggerDispatcher.info("pilotageListIdsource", LOGGER_APISERVICE);
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT container, "+ColumnEnum.ID_SOURCE.getColumnName()+" FROM " + tablePilotage + " ");
		requete.append("WHERE phase_traitement=" + requete.quoteText(aCurrentPhase) + " ");
		requete.append("AND " + requete.quoteText(etat) + "=ANY(etat_traitement); ");
		try {
			return new GenericBean(UtilitaireDao.get(0).executeRequest(this.connexion.getCoordinatorConnection(), requete)).mapContent();
		} catch (ArcException ex) {
			LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "pilotageListIdSource()", ex);
		}
		return new HashMap<>();
	}

	/**
	 * Marque la phase et l'état d'un idsource dans une table de pilotage
	 *
	 * @param tablePilotage
	 * @param idSource
	 * @param phaseNew
	 * @param etatNew
	 * @return
	 */
	public static StringBuilder pilotageMarkIdsource(String tablePilotage, String idSource, String phaseNew,
			String etatNew, String rapport, String... jointure) {
		StringBuilder requete = new StringBuilder();
		requete.append("UPDATE " + tablePilotage + " ");
		requete.append("SET phase_traitement= '" + phaseNew + "' ");
		requete.append(", etat_traitement= '{" + etatNew + "}' ");
		if (rapport == null) {
			requete.append(", rapport= null ");
		} else {
			requete.append(", rapport= '" + rapport + "' ");
		}

		if (jointure.length > 0) {
			requete.append(", jointure= '" + jointure[0] + "'");
		}

		requete.append("WHERE "+ColumnEnum.ID_SOURCE.getColumnName()+"='" + idSource + "';\n");
		return requete;
	}


	/**
	 * Requête de sélection de la liste des colonnes des tables métier associée à
	 * une norme
	 *
	 * @param listeTable
	 * @return
	 */
	public static ArcPreparedStatementBuilder listeColonneTableMetierSelonFamilleNorme(String anEnvironnement,
			String idFamille) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

		requete.append("SELECT DISTINCT nom_variable_metier, type_variable_metier\n")
				.append("  FROM " + anEnvironnement + "_mod_variable_metier\n")
				.append("  WHERE lower(id_famille)=lower(" + requete.quoteText(idFamille) + ")");

		return requete;
	}

	/**
	 * Return the query that marks the files or all file if idSource not provided
	 * The mark indicates reset etape to 0 for the previous phase, meaning the file
	 * is no longer processed in the current phase
	 * 
	 * @param idSource
	 * @return
	 */
	public static StringBuilder resetPreviousPhaseMark(String tablePil, String idSource, String tableSource) {
		StringBuilder requete = new StringBuilder();

		// mettre à etape = 0 la phase marquée à 3
		requete.append("\n UPDATE " + tablePil + " a ");
		requete.append("\n SET etape=0 ");
		requete.append("\n WHERE a.etape=3 ");
		if (idSource != null) {
			requete.append("\n AND a."+ColumnEnum.ID_SOURCE.getColumnName()+" = '" + idSource + "' ");
		}

		if (tableSource != null) {
			requete.append("\n AND EXISTS (SELECT 1 FROM " + tableSource + " b where a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+") ");
		}

		requete.append("\n ;");
		return requete;
	}


	/**
	 *
	 * @return le temps d'execution
	 */
	public ServiceReporting invokeApi() {
		double start = System.currentTimeMillis();

		loggerDispatcher.info("****** Execution " + this.getCurrentPhase() + " *******", LOGGER_APISERVICE);
		try {

			if (this.getCurrentPhase().equals(TraitementPhase.INITIALISATION.toString())
					|| this.getCurrentPhase().equals(TraitementPhase.RECEPTION.toString())) {
				this.todo = true;
			} else {
				this.todo = checkTodo(this.getTablePil(), this.getPreviousPhase(), this.getCurrentPhase());
			}
			loggerDispatcher.info("A faire - " + this.getCurrentPhase() + " : " + this.todo, LOGGER_APISERVICE);

			if (this.initialiser()) {
				try {
					this.executer();
				} catch (ArcException ex) {
					loggerDispatcher.error("Erreur dans " + this.getCurrentPhase() + ". ", ex, LOGGER_APISERVICE);
					try {
						this.repriseSurErreur(this.connexion.getCoordinatorConnection(), this.getCurrentPhase(), this.getTablePil(), ex,
								"aucuneTableADroper");
					} catch (Exception ex2) {
						loggerDispatcher.error("Error in ApiService.invokeApi.repriseSurErreur", LOGGER_APISERVICE);
					}
				}
			}
		} finally {
			this.finaliser();
		}

		loggerDispatcher.info("****** Fin " + this.getCurrentPhase() + " *******", LOGGER_APISERVICE);

		return new ServiceReporting(this.reportNumberOfObject, System.currentTimeMillis() - start);

	}


	public String getTablePilTemp() {
		return this.tablePilTemp;
	}

	public void setTablePilTemp(String tablePilTemp) {
		this.tablePilTemp = tablePilTemp;
	}

	/**
	 * Remise dans l'état juste avant le lancement des controles et insertion dans
	 * une table d'erreur
	 *
	 * @param connexion
	 * @param phase
	 * @param tablePil
	 * @param exception
	 * @param tableDrop
	 * @throws ArcException
	 */
	private void repriseSurErreur(Connection connexion, String phase, String tablePil, ArcException exception,
			String... tableDrop) throws ArcException {
		// nettoyage de la connexion
		// comme on arrive ici à cause d'une erreur, la base de donnée attend une fin de
		// la transaction
		// si on lui renvoie une requete SQL, il la refuse avec le message
		// ERROR: current transaction is aborted, commands ignored until end of
		// transaction block
		try {
			this.connexion.getCoordinatorConnection().setAutoCommit(false);
			this.connexion.getCoordinatorConnection().rollback();
		} catch (SQLException rollbackException) {
			throw new ArcException(rollbackException, ArcExceptionMessage.DATABASE_ROLLBACK_FAILED);
		}
		StringBuilder requete = new StringBuilder();

		for (int i = 0; i < tableDrop.length; i++) {
			requete.append("DROP TABLE IF EXISTS " + tableDrop[i] + ";");
		}

		requete.append("WITH t0 AS ( ");
		requete.append(ServicePilotageOperation.updatePilotageErrorQuery(phase, tablePil, exception));
		requete.append("\n RETURNING "+ColumnEnum.ID_SOURCE.getColumnName()+") ");

		requete.append(resetPreviousPhaseMark(tablePil, null, "t0"));

		UtilitaireDao.get(0).executeBlock(connexion, requete);
	}

	/**
	 * Remise dans l'état juste avant le lancement des controles et insertion dans
	 * une table d'erreur pour un fichier particulier
	 *
	 * @param connexion
	 * @param phase
	 * @param tablePil
	 * @param exception
	 * @param tableDrop
	 * @throws ArcException
	 */
	public void repriseSurErreur(Connection connexion, String phase, String tablePil, String idSource,
			ArcException exception, String... tableDrop) throws ArcException {
		// nettoyage de la connexion
		// comme on arrive ici à cause d'une erreur, la base de donnée attend une fin de
		// la transaction
		// si on lui renvoie une requete SQL, il la refuse avec le message
		// ERROR: current transaction is aborted, commands ignored until end of
		// transaction block
		try {
			this.connexion.getCoordinatorConnection().setAutoCommit(false);
			this.connexion.getCoordinatorConnection().rollback();
		} catch (SQLException rollbackException) {
			throw new ArcException(rollbackException, ArcExceptionMessage.DATABASE_ROLLBACK_FAILED);
		}

		// promote the application user account to full right
		UtilitaireDao.get(0).executeImmediate(connexion, switchToFullRightRole());

		StringBuilder requete = new StringBuilder();

		for (int i = 0; i < tableDrop.length; i++) {
			requete.append("DROP TABLE IF EXISTS " + tableDrop[i] + ";");
		}
		requete.append(ServicePilotageOperation.updatePilotageErrorQuery(phase, tablePil, exception));

		requete.append("\n AND "+ColumnEnum.ID_SOURCE.getColumnName()+" = '" + idSource + "' ");
		requete.append("\n ;");

		requete.append(resetPreviousPhaseMark(tablePil, idSource, null));

		UtilitaireDao.get(0).executeBlock(connexion, requete);
	}


	/**
	 * permet de récupérer un tableau de la forme id_source | id1 , id2, id3 ...
	 * type_comp | comp1,comp2, comp3 ...
	 * 
	 * @return
	 * @throws ArcException
	 */
	protected HashMap<String, ArrayList<String>> recuperationIdSource() throws ArcException {
		
		ArcPreparedStatementBuilder query=new ArcPreparedStatementBuilder();
		query.append("SELECT p."+ColumnEnum.ID_SOURCE.getColumnName()+" ");
		query.append("FROM " + this.getTablePilTemp() + " p ");
		query.append("ORDER BY "+ColumnEnum.ID_SOURCE.getColumnName());
		query.append(";");
		
		HashMap<String, ArrayList<String>> pil = new GenericBean(
				UtilitaireDao.get(0)
						.executeRequest(this.connexion.getCoordinatorConnection(), query ))
										.mapContent();

		return (pil);

	}

	public String getEnvExecution() {
		return envExecution;
	}

	public void setEnvExecution(String envExecution) {
		this.envExecution = envExecution;
	}

	public HashMap<String, ArrayList<String>> getTabIdSource() {
		return tabIdSource;
	}

	protected void setTabIdSource(HashMap<String, ArrayList<String>> tabIdSource) {
		this.tabIdSource = tabIdSource;
	}

	public String getTablePil() {
		return tablePil;
	}

	public void setTablePil(String tablePil) {
		this.tablePil = tablePil;
	}

	public String getPreviousPhase() {
		return previousPhase;
	}

	public void setPreviousPhase(String previousPhase) {
		this.previousPhase = previousPhase;
	}

	public String getCurrentPhase() {
		return currentPhase;
	}

	public void setCurrentPhase(String currentPhase) {
		this.currentPhase = currentPhase;
	}

	public String getTablePrevious() {
		return tablePrevious;
	}

	public void setTablePrevious(String tablePrevious) {
		this.tablePrevious = tablePrevious;
	}

	public String getParamBatch() {
		return paramBatch;
	}

	protected void setParamBatch(String paramBatch) {
		this.paramBatch = paramBatch;
	}

	public String getTableJeuDeRegle() {
		return tableJeuDeRegle;
	}

	public void setTableJeuDeRegle(String tableJeuDeRegle) {
		this.tableJeuDeRegle = tableJeuDeRegle;
	}

	public String getTableNorme() {
		return tableNorme;
	}

	public void setTableNorme(String tableNorme) {
		this.tableNorme = tableNorme;
	}

	public String getTableOutKo() {
		return tableOutKo;
	}

	public void setTableOutKo(String tableOutKo) {
		this.tableOutKo = tableOutKo;
	}

	public ScalableConnection getConnexion() {
		return connexion;
	}

	public String getTableControleRegle() {
		return tableControleRegle;
	}

	public String getTableChargementRegle() {
		return tableChargementRegle;
	}

	public void setTableChargementRegle(String tableChargementRegle) {
		this.tableChargementRegle = tableChargementRegle;
	}

	public void setTableControleRegle(String tableControleRegle) {
		this.tableControleRegle = tableControleRegle;
	}

	public String getTableMappingRegle() {
		return tableMappingRegle;
	}

	public void setTableMappingRegle(String tableMappingRegle) {
		this.tableMappingRegle = tableMappingRegle;
	}

	public Integer getNbEnr() {
		return nbEnr;
	}

	public void setNbEnr(Integer nbEnr) {
		this.nbEnr = nbEnr;
	}

	public String getTableNormageRegle() {
		return tableNormageRegle;
	}

	public void setTableNormageRegle(String tableNormageRegle) {
		this.tableNormageRegle = tableNormageRegle;
	}

	public String getDirectoryRoot() {
		return directoryRoot;
	}

	public void setDirectoryRoot(String directoryRoot) {
		this.directoryRoot = directoryRoot;
	}

	/**
	 * @return the idSource
	 */
	public String getIdSource() {
		return idSource;
	}

	/**
	 * @param idSource the idSource to set
	 */
	public void setIdSource(String idSource) {
		this.idSource = idSource;
	}

	public int getReportNumberOfObject() {
		return reportNumberOfObject;
	}

	public void setReportNumberOfObject(int reportNumberOfObject) {
		this.reportNumberOfObject = reportNumberOfObject;
	}

	public String getDirectoryIn() {
		return directoryIn;
	}

	public void setDirectoryIn(String directoryIn) {
		this.directoryIn = directoryIn;
	}

	public List<Norme> getListeNorme() {
		return listeNorme;
	}

	public void setListeNorme(List<Norme> listeNorme) {
		this.listeNorme = listeNorme;
	}

	
	
}
