package fr.insee.arc.core.service;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.model.NormeFichier;
import fr.insee.arc.core.model.ServiceReporting;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementTableExecution;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.ModeRequeteImpl;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.ressourceUtils.SpringApplicationContext;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

@Component
public abstract class ApiService implements IDbConstant, IConstanteNumerique {

	protected static final Logger LOGGER_APISERVICE = LogManager.getLogger(ApiService.class);

	public static final String FICHIER_MISE_EN_PRODUCTION = "production.dummy";

	protected int maxParallelWorkers;

	public static final String CHILD_TABLE_TOKEN = "child";

	// racine xml
	public static final String ROOT = "root";

	// anti-spam delay when thread chain error
	protected static final int PREVENT_ERROR_SPAM_DELAY = 100;

	@Autowired
	protected PropertiesHandler properties;

	@Autowired
	@Qualifier("activeLoggerDispatcher")
	protected LoggerDispatcher loggerDispatcher;

	protected Connection connexion;
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
	protected String tableFiltrageRegle;
	protected String tableMappingRegle;
	protected String tableControleRegle;
	protected String tableSeuil;
	protected Integer nbEnr;
	protected String tableCalendrier;
	protected String directoryRoot;
	protected String nullString = "[[[#NULL VALUE#]]]";
	protected String paramBatch = null;
	protected String currentIdSource;
	
	// made to report the number of object processed by the phase
	private int reportNumberOfObject = 0;

	protected String bdDateFormat = "DD/MM/YYYY HH24:MI:SS";

	public static final String IHM_SCHEMA = "arc.ihm";

	protected String idSource;

	protected Boolean todo = false;

	private HashMap<String, ArrayList<String>> tabIdSource;

	public Exception error = null;
	public Thread t = null;

	/**
	 * Build the connection pool for mutithreading
	 * returns a list of connections usable by the threads 
	 * @param parallel
	 * @param connexion
	 * @param anEnvExecution
	 * @param restrictedUsername
	 * @return
	 */
	public static ArrayList<Connection> prepareThreads(int parallel, Connection connexion, String anEnvExecution,
			String restrictedUsername) {
		ArrayList<Connection> connexionList = new ArrayList<>();
		try {
			if (connexion != null) {
				connexionList.add(connexion);
				UtilitaireDao.get("arc").executeImmediate(connexion, configConnection(anEnvExecution));

			}

			for (int i = connexionList.size(); i < parallel; i++) {

				Connection connexionTemp = UtilitaireDao.get(poolName).getDriverConnexion();
				connexionList.add(connexionTemp);

				// demote application user account to temporary restricted operations and
				// readonly or non-temporary schema
				UtilitaireDao.get("arc").executeImmediate(connexionTemp, configConnection(anEnvExecution)
						+ (restrictedUsername.equals("") ? "" : FormatSQL.changeRole(restrictedUsername)));
			}

		} catch (Exception ex) {
			LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "prepareThreads()", ex);
		}
		return connexionList;

	}

	public void waitForThreads2(int parallel, ArrayList<? extends ApiService> threadList,
			ArrayList<Connection> connexionList) throws ArcException {

		while (threadList.size() >= parallel && !threadList.isEmpty()) {
			Iterator<? extends ApiService> it = threadList.iterator();

			while (it.hasNext()) {
				ApiService px = it.next();
				if (!px.getT().isAlive()) {

					if (px.getError() != null) {
						error = px.error;
					}
					it.remove();

					// close connexion when thread is done except if it is the master connexion
					// (first one)
					if (parallel == 0 && !px.getConnexion().equals(connexionList.get(0))) {
						try {
							px.getConnexion().close();
						} catch (SQLException e) {
							throw new ArcException("Error in closing thread connection",e);
						}
					}

				}
			}
		}
	}

	/**
	 * @param connextionThread
	 * @param threadList
	 * @param connexionList
	 * @return
	 */
	public Connection chooseConnection(Connection connextionThread, ArrayList<? extends ApiService> threadList,
			ArrayList<Connection> connexionList) {
		// on parcourt l'array list de this.connexion disponible
		for (int i = 0; i < connexionList.size(); i++) {
			boolean choosen = true;

			for (int j = 0; j < threadList.size(); j++) {
				if (connexionList.get(i).equals(threadList.get(j).getConnexion())) {
					choosen = false;
				}
			}

			if (choosen) {
				connextionThread = connexionList.get(i);
				break;
			}
		}
		return connextionThread;
	}

	/**
	 * Permet la rétro compatibilité pour la migration vers 1 schéma par
	 * envirionnement d'execution
	 * 
	 * @param anEnv
	 * @return
	 */
	public static String dbEnv(String env) {
		return env.replace(".", "_") + ".";
	}

	public ApiService() {
		super();
		springInit();
	}

	public ApiService(String aCurrentPhase, String aParametersEnvironment, String aEnvExecution, String aDirectoryRoot,
			Integer aNbEnr, String... paramBatch) {
		this();
		loggerDispatcher.info("** initialiserVariable **", LOGGER_APISERVICE);
		try {
			this.connexion = UtilitaireDao.get(poolName).getDriverConnexion();
		} catch (Exception ex) {
			LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "ApiService()", ex);
		}

		if (paramBatch != null && paramBatch.length > 0) {
			this.setParamBatch(paramBatch[0]);
		}

		// Initialisation de la phase
		this.setCurrentPhase(aCurrentPhase);
		this.setPreviousPhase(TraitementPhase.valueOf(this.getCurrentPhase()).previousPhase().toString());
		// Table en entrée
		this.setEnvExecution(aEnvExecution);
		this.envParameters = aParametersEnvironment;
		this.setDirectoryRoot(aDirectoryRoot);

		this.setTablePrevious((dbEnv(aEnvExecution) + this.getPreviousPhase() + "_" + TraitementEtat.OK).toLowerCase());

		// Tables de pilotage et pilotage temporaire
		this.setTablePil(dbEnv(aEnvExecution) + TraitementTableExecution.PILOTAGE_FICHIER);
		this.tablePilTemp = temporaryTableName(aEnvExecution, aCurrentPhase,
				TraitementTableExecution.PILOTAGE_FICHIER.toString(), "0");
		this.setTableNorme(dbEnv(aEnvExecution) + TraitementTableParametre.NORME);
		this.tableCalendrier = dbEnv(aEnvExecution) + TraitementTableParametre.CALENDRIER;
		// Tables venant de l'initialisation globale
		this.setTableJeuDeRegle(dbEnv(aEnvExecution) + TraitementTableParametre.JEUDEREGLE);
		this.setTableChargementRegle(dbEnv(aEnvExecution) + TraitementTableParametre.CHARGEMENT_REGLE);
		this.setTableNormageRegle(dbEnv(aEnvExecution) + TraitementTableParametre.NORMAGE_REGLE);
		this.setTableControleRegle(dbEnv(aEnvExecution) + TraitementTableParametre.CONTROLE_REGLE);
		this.setTableFiltrageRegle(dbEnv(aEnvExecution) + TraitementTableParametre.FILTRAGE_REGLE);
		this.setTableMappingRegle(dbEnv(aEnvExecution) + TraitementTableParametre.MAPPING_REGLE);
		this.setTableSeuil(dbEnv(aEnvExecution) + TraitementTableParametre.SEUIL);
		this.setTableOutKo((dbEnv(aEnvExecution) + this.getCurrentPhase() + "_" + TraitementEtat.KO).toLowerCase());
		this.setNbEnr(aNbEnr);

		loggerDispatcher.info("** Fin constructeur ApiService **", LOGGER_APISERVICE);
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
				UtilitaireDao.get(poolName).executeBlock(this.connexion, configConnection());
			} catch (ArcException ex) {
				LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "initialiser()", ex);
			}
			register(this.connexion, this.getPreviousPhase(), this.getCurrentPhase(), this.getTablePil(),
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
	 * Permet de configurer la connexion Mettre un timeout par exemple
	 */
	private StringBuilder configConnection() {
		return configConnection(this.getEnvExecution());
	}

	private static StringBuilder configConnection(String anEnvExecution) {
		StringBuilder requete = new StringBuilder();
		requete.append(ModeRequeteImpl.arcModeRequeteEngine(ManipString.substringBeforeFirst(ApiService.dbEnv(anEnvExecution), ".")));
		return requete;

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
		boolean todo = false;
		requete.append("SELECT 1 FROM " + tablePil + " a ");
		requete.append("WHERE phase_traitement=" + requete.quoteText(phaseAncien) + " AND "
				+ requete.quoteText(TraitementEtat.OK.toString()) + "=ANY(etat_traitement) ");
		requete.append("and etape=1 ");
		requete.append("limit 1 ");
		try {
			todo = UtilitaireDao.get(poolName).hasResults(this.connexion, requete);
		} catch (Exception ex) {
			LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "checkTodo()", ex);
		}
		return todo;
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
			UtilitaireDao.get(poolName).executeBlock(connexion,
					copieTablePilotage(phase, tablePil, tablePilTemp, phaseIn, phase, nbEnr));
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
	 * Requete permettant de récupérer les règles pour un id_source donnée et une
	 * table de regle
	 * 
	 * @param idSource      : identifiant du fichier
	 * @param tableRegle    : table de regle
	 * @param tablePilotage : table de pilotage
	 * @return
	 */
	public static String getRegles(String tableRegle, String tablePilotage) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT * FROM " + tableRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		return requete.toString();
	}

	public static String getRegles(String tableRegle, NormeFichier normeFichier) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT * FROM " + tableRegle + " a WHERE ");
		requete.append(conditionRegle(normeFichier));
		return requete.toString();
	}

	/**
	 * Récupère toutes les rubriques utilisées dans les regles relatives au fichier
	 * 
	 * @param idSource
	 * @param tablePilotage
	 * @param tableNormageRegle
	 * @param tableControleRegle
	 * @param tableFiltrageRegle
	 * @param tableMappingRegle
	 * @return
	 */
	public static String getAllRubriquesInRegles(String tablePilotage, String tableNormageRegle,
			String tableControleRegle, String tableFiltrageRegle, String tableMappingRegle) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT * FROM ( ");
		requete.append(
				"\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(expr_regle_col),'{([iv]_{1,1}[^{}]+)}','g')) as var from "
						+ tableMappingRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append(
				"\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(expr_regle_filtre),'{([iv]_{1,1}[^{}]+)}','g')) as var from "
						+ tableFiltrageRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_pere) as var from "
				+ tableControleRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_fils) as var from "
				+ tableControleRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append(
				"\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(condition),'{([iv]_{1,1}[^{}]+)}','g')) as var from "
						+ tableControleRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append(
				"\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(pre_action),'{([iv]_{1,1}[^{}]+)}','g')) as var from "
						+ tableControleRegle + " a WHERE ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique) as var from "
				+ tableNormageRegle + " a where id_classe!='suppression' AND ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n UNION ");
		requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_nmcl) as var from "
				+ tableNormageRegle + " a where id_classe!='suppression' AND ");
		requete.append(conditionRegle(tablePilotage));
		requete.append("\n ) ww where var is NOT NULL; ");
		return requete.toString();
	}

	/**
	 * Retourne la clause WHERE SQL qui permet de selectionne les bonne regles pour
	 * un fichier
	 * 
	 * @param idSource
	 * @param tablePilotage
	 * @return
	 */
	private static String conditionRegle(String tablePilotage) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n ");
		requete.append("EXISTS ( SELECT * FROM " + tablePilotage + " b ");
		requete.append("WHERE a.id_norme=b.id_norme ");
		requete.append("AND a.periodicite=b.periodicite ");
		requete.append("AND a.validite_inf<=to_date(b.validite,'YYYY-MM-DD') ");
		requete.append("AND a.validite_sup>=to_date(b.validite,'YYYY-MM-DD') ");
		requete.append(") ");
		return requete.toString();
	}

	private static String conditionRegle(NormeFichier normeFichier) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n ");
		requete.append("a.id_norme='" + normeFichier.getIdNorme() + "' ");
		requete.append("AND a.periodicite='" + normeFichier.getPeriodicite() + "' ");
		requete.append("AND a.validite_inf<=to_date('" + normeFichier.getValidite() + "','YYYY-MM-DD') ");
		requete.append("AND a.validite_sup>=to_date('" + normeFichier.getValidite() + "','YYYY-MM-DD') ");
		requete.append(";");
		return requete.toString();
	}

	/**
	 * Requete permettant de récupérer les règles pour un id_source donnée et une
	 * table de regle
	 * 
	 * @param id_source
	 * @param tableRegle
	 * @return SQL pil.id_source, pil.jointure, pil.id_norme, pil.validite,
	 *         pil.periodicite, pil.validite
	 */
	public static String getNormeAttributes(String idSource, String tablePilotage) {
		StringBuilder requete = new StringBuilder();
		requete.append(
				"\n SELECT pil."+ColumnEnum.ID_SOURCE.getColumnName()+", pil.jointure, pil.id_norme, pil.validite, pil.periodicite, pil.validite "
						+ "FROM " + tablePilotage + " pil " + " WHERE "+ColumnEnum.ID_SOURCE.getColumnName()+"='" + idSource + "' ");
		return requete.toString();
	}

	/**
	 * récupere le contenu d'une requete dans un map
	 * 
	 * @param c
	 * @param req
	 * @return
	 * @throws ArcException
	 */
	public static HashMap<String, ArrayList<String>> getBean(Connection c, String req) throws ArcException {
		GenericBean gb = new GenericBean(UtilitaireDao.get("arc").executeRequest(c, new ArcPreparedStatementBuilder(req)));
		return gb.mapContent(true);
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

	/**
	 * Selection d'un lot d'id_source pour appliquer le traitement Les id_sources
	 * sont selectionnés parmi les id_source présent dans la phase précédentes avec
	 * etape =1 Ces id_source sont alors mis à jour dans la phase précédente à étape
	 * =0 et une nouvelle ligne est créee pour la phase courante et pour chaque
	 * id_source avec etape=1 Fabrique une copie de la table de pilotage avec
	 * uniquement les fichiers concernés par le traitement
	 * 
	 * @param phase
	 * @param tablePil
	 * @param tablePilTemp
	 * @param phaseAncien
	 * @param phaseNouveau
	 * @param nbEnr
	 * @return
	 */
	private String copieTablePilotage(String phase, String tablePil, String tablePilTemp, String phaseAncien,
			String phaseNouveau, Integer nbEnr) {
		StringBuilder requete = new StringBuilder();

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		requete.append("\n DROP TABLE IF EXISTS " + tablePilTemp + "; ");

		requete.append("\n CREATE ");
		if (!tablePilTemp.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append(" ");
		}
		requete.append("\n TABLE " + tablePilTemp
				+ " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS  ");

		requete.append("\n WITH prep AS (");
		requete.append("\n SELECT a.*, count(1) OVER (ORDER BY date_traitement, nb_essais, "+ColumnEnum.ID_SOURCE.getColumnName()+") as cum_enr ");
		requete.append("\n FROM " + tablePil + " a ");
		requete.append("\n WHERE phase_traitement='" + phaseAncien + "'  AND '" + TraitementEtat.OK
				+ "'=ANY(etat_traitement) and etape=1 ) ");
		requete.append("\n , mark AS (SELECT a.* FROM prep a WHERE cum_enr<" + nbEnr + " ");
		requete.append("\n UNION   (SELECT a.* FROM prep a LIMIT 1)) ");

		// update the line in pilotage with etape=3 for the previous step
		requete.append("\n , update as ( UPDATE " + tablePil
				+ " a set etape=3 from mark b where a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+" and a.etape=1 AND a.phase_traitement='"
				+ phaseAncien + "'  AND '" + TraitementEtat.OK + "'=ANY(a.etat_traitement)) ");

		// insert the line in pilotage with etape=1 for the current step
		requete.append("\n , insert as (INSERT INTO " + tablePil + " ");
		requete.append(
				"\n (container, "+ColumnEnum.ID_SOURCE.getColumnName()+", date_entree, id_norme, validite, periodicite, phase_traitement, etat_traitement, date_traitement, rapport, taux_ko, nb_enr, nb_essais, etape, generation_composite,jointure) ");
		requete.append("\n SELECT container, "+ColumnEnum.ID_SOURCE.getColumnName()+", date_entree, id_norme, validite, periodicite, '" + phaseNouveau
				+ "' as phase_traitement, '{" + TraitementEtat.ENCOURS + "}' as etat_traitement ");
		requete.append("\n , to_timestamp('" + formatter.format(date) + "','" + this.bdDateFormat
				+ "') , rapport, taux_ko, nb_enr, nb_essais, 1 as etape, generation_composite, jointure ");
		requete.append("\n FROM mark ");
		requete.append("\n RETURNING *) ");

		requete.append("\n SELECT * from insert; ");
		requete.append("\n ANALYZE " + tablePilTemp + ";");
		return requete.toString();
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

				if (!(this.getTablePrevious().contains(TraitementPhase.DUMMY.toString().toLowerCase())
						|| this.getTablePrevious().contains(TraitementPhase.INITIALISATION.toString().toLowerCase())
						|| this.getTablePrevious().contains(TraitementPhase.RECEPTION.toString().toLowerCase()))) {
					deleteTodo(this.connexion, this.tablePilTemp, this.getTablePrevious(), this.paramBatch);
				}
				StringBuilder requete = new StringBuilder();
				requete.append(FormatSQL.dropTable(this.tablePilTemp));
				try {
					UtilitaireDao.get(poolName).executeBlock(this.connexion, requete);
				} catch (Exception ex) {
					LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "finaliser()", ex);
				}
			}
		} finally {
			try {
				if (this.connexion != null) {
					this.connexion.close();
					this.connexion = null;
				}
			} catch (Exception ex) {
				LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "finaliser()", ex);
			}

		}
	}

	/**
	 * Effacer les fichiers traités de la table to_do de la phase précédente
	 * 
	 * @param connexion
	 * @param tablePilTemp
	 * @param tablePrevious
	 */
	private static void deleteTodo(Connection connexion, String tablePilTemp, String tablePrevious, String paramBatch) {
		try {

			// Si on est en batch, on drop les tables source
			// sinon on retire le lien avec la table héritée
			StringBuilder query = new StringBuilder();
			HashMap<String, ArrayList<String>> m = new GenericBean(UtilitaireDao.get(poolName).executeRequest(connexion,
					new ArcPreparedStatementBuilder("select "+ColumnEnum.ID_SOURCE.getColumnName()+" from " + tablePilTemp + ""))).mapContent();
			int count = 0;
			for (String z : m.get(ColumnEnum.ID_SOURCE.getColumnName())) {

				count++;
				if (paramBatch != null) {
					query.append("DROP TABLE IF EXISTS " + tableOfIdSource(tablePrevious, z) + ";");

				}
				if (count > FormatSQL.MAX_LOCK_PER_TRANSACTION) {
					UtilitaireDao.get(poolName).executeBlock(connexion, query);
					query.setLength(0);
					count = 0;
				}
			}
			UtilitaireDao.get(poolName).executeBlock(connexion, query);

		} catch (Exception ex) {
			LoggerHelper.error(LOGGER_APISERVICE, ApiService.class, "deleteTodo()", ex);
		}

	}

	/**
	 * Maintenance sur la table de pilotage
	 * 
	 * @param connexion
	 * @param envExecution
	 * @param type
	 */
	private static void maintenancePilotage(Connection connexion, String envExecution, String type) {
		String tablePil = dbEnv(envExecution) + TraitementTableExecution.PILOTAGE_FICHIER;
		StaticLoggerDispatcher.info("** Maintenance Pilotage **", LOGGER_APISERVICE);

		try {
			UtilitaireDao.get(poolName).executeImmediate(connexion, FormatSQL.analyzeSecured(tablePil));
			UtilitaireDao.get(poolName).executeImmediate(connexion, FormatSQL.vacuumSecured(tablePil, type));
		} catch (Exception e) {
			StaticLoggerDispatcher.error("Error in ApiService.maintenancePilotage", LOGGER_APISERVICE);
		}
	}

/**
 * 
 * @param connexion
 * @param type
 */
	public static void maintenancePgCatalog(Connection connexion, String type) {
		// postgres libere mal l'espace sur ces tables qaund on fait trop d'opération
		// sur les colonnes
		// vaccum full sinon ca fait quasiment rien ...
		StaticLoggerDispatcher.info("** Maintenance Catalogue **", LOGGER_APISERVICE);
		UtilitaireDao.get(poolName).maintenancePgCatalog(connexion, type);
	}

	/**
	 * classic database maintenance routine 2 vacuum are sent successively to
	 * analyze and remove dead tuple completely from
	 * 
	 * @param connexion    the jdbc connexion
	 * @param envExecution the sandbox schema
	 */
	public static void maintenanceDatabaseClassic(Connection connexion, String envExecution) {
		ApiService.maintenanceDatabase(connexion, envExecution, FormatSQL.VACUUM_OPTION_NONE);
	}

	/**
	 * analyze and vacuum on postgres catalog tables analyze and vacuum on the
	 * pilotage table located in the sandbox schema
	 * 
	 * @param connexion       the jdbc connexion
	 * @param envExecution    the sandbox schema
	 * @param typeMaintenance FormatSQL.VACUUM_OPTION_FULL or
	 *                        FormatSQL.VACUUM_OPTION_NONE
	 */
	private static void maintenanceDatabase(Connection connexion, String envExecution, String typeMaintenance) {
		ApiService.maintenancePgCatalog(connexion, typeMaintenance);

		ApiService.maintenancePilotage(connexion, envExecution, typeMaintenance);

		StaticLoggerDispatcher.info("** Fin de maintenance **", LOGGER_APISERVICE);
	}

	/**
	 * Build a signifiant and collision free temporary table name
	 * 
	 * @param aEnvExecution the sandbox schema
	 * @param aCurrentPhase the phase TraitementPhase that will be used as part of
	 *                      the builded name
	 * @param tableName     the based tablename that will be used as part of the
	 *                      builded name
	 * @param suffix        optionnal suffix added to the temporary name
	 * @return
	 */
	private static String temporaryTableName(String aEnvExecution, String aCurrentPhase, String tableName,
			String... suffix) {

		if (suffix != null && suffix.length > 0) {
			String suffixJoin = String.join("$", suffix);
			return FormatSQL.temporaryTableName(dbEnv(aEnvExecution) + aCurrentPhase + "_" + tableName, suffixJoin);
		} else {
			return FormatSQL.temporaryTableName(dbEnv(aEnvExecution) + aCurrentPhase + "_" + tableName);
		}
	}

	public static String globalTableName(String aEnvExecution, String aCurrentPhase, String tableName) {
		return dbEnv(aEnvExecution) + aCurrentPhase + "_" + tableName;
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
			return new GenericBean(UtilitaireDao.get(poolName).executeRequest(this.connexion, requete)).mapContent();
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
	 * return the query that marks the file processed by the phase in the persistent
	 * pilotage table @param tablePil
	 * 
	 * @param tablePil
	 * @param tablePilTemp
	 * @param idSource
	 * @return
	 */
	public String marquageFinal(String tablePil, String tablePilTemp, String idSource) {
		StringBuilder requete = new StringBuilder();
		Date date = new Date();

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		requete.append("\n set enable_hashjoin=off; ");
		requete.append("\n UPDATE " + tablePil + " a ");
		requete.append("\n \t SET etat_traitement =  b.etat_traitement, ");
		requete.append("\n \t   id_norme = b.id_norme, ");
		requete.append("\n \t   validite = b.validite, ");
		requete.append("\n \t   periodicite = b.periodicite, ");
		requete.append("\n \t   taux_ko = b.taux_ko, ");
		requete.append("\n \t   date_traitement = to_timestamp('" + formatter.format(date) + "','" + this.bdDateFormat
				+ "'), ");
		requete.append("\n \t   nb_enr = b.nb_enr, ");
		requete.append("\n \t   rapport = b.rapport, ");
		requete.append("\n \t   validite_inf = b.validite_inf, ");
		requete.append("\n \t   validite_sup = b.validite_sup, ");
		requete.append("\n \t   version = b.version, ");
		requete.append(
				"\n \t   etape = case when b.etat_traitement='{" + TraitementEtat.KO + "}' then 2 else b.etape end, ");
		requete.append("\n \t   jointure = b.jointure ");

		// Si on dispose d'un id source on met à jour seulement celui ci
		requete.append("\n \t FROM " + tablePilTemp + " as b ");
		requete.append("\n \t WHERE a."+ColumnEnum.ID_SOURCE.getColumnName()+" = '" + idSource + "' ");
		requete.append("\n \t AND a.etape = 1 ; ");

		requete.append(resetPreviousPhaseMark(tablePil, idSource, null));

		requete.append("\n set enable_hashjoin = on; ");
		return requete.toString();

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
	 * Créer une table image vide d'une autre table Si le schema est spécifié, la
	 * table est créée dans le schema; sinon elle est crée en temporary
	 *
	 * @param tableIn
	 * @param tableToBeCreated
	 * @return
	 */
	public static String creationTableResultat(String tableIn, String tableToBeCreated, Boolean... image) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n CREATE ");
		if (!tableToBeCreated.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append(" ");
		}
		requete.append("TABLE " + tableToBeCreated + " ");
		requete.append("" + FormatSQL.WITH_NO_VACUUM + " ");
		requete.append("as SELECT * FROM " + tableIn + " ");
		if (image.length == 0 || image[0] == false) {
			requete.append("where 1=0 ");
		}
		requete.append("; ");
		return requete.toString();
	}

	/**
	 * Directory management
	 */
	private static final String DIRECTORY_EXPORT_QUALIFIIER = "EXPORT";

	private static final String DIRECTORY_TOKEN = "_";

	private static final String DIRECTORY_ARCHIVE_QUALIFIIER = "ARCHIVE";

	private static final String DIRECTORY_OLD_QUALIFIIER = "OLD";

	public static String directoryEnvRoot(String rootDirectory, String env) {
		return rootDirectory + File.separator + env.replace(".", "_").toUpperCase();
	}

	public static String directoryPhaseRoot(String rootDirectory, String env, TraitementPhase t) {
		return directoryEnvRoot(rootDirectory, env) + File.separator + t.toString();
	}

	public static String directoryEnvExport(String rootDirectory, String env) {
		return directoryEnvRoot(rootDirectory, env) + File.separator + DIRECTORY_EXPORT_QUALIFIIER;
	}

	public static String directoryPhaseEntrepot(String rootDirectory, String env, TraitementPhase t, String entrepot) {
		return directoryPhaseRoot(rootDirectory, env, t) + DIRECTORY_TOKEN + entrepot;
	}

	public static String directoryPhaseEntrepotArchive(String rootDirectory, String env, TraitementPhase t,
			String entrepot) {
		return directoryPhaseEntrepot(rootDirectory, env, t, entrepot) + DIRECTORY_TOKEN + DIRECTORY_ARCHIVE_QUALIFIIER;
	}

	public static String directoryPhaseEntrepotArchiveOld(String rootDirectory, String env, TraitementPhase t,
			String entrepot) {
		return directoryPhaseEntrepotArchive(rootDirectory, env, t, entrepot) + File.separator
				+ DIRECTORY_OLD_QUALIFIIER;
	}

	public static String directoryPhaseEtat(String rootDirectory, String env, TraitementPhase t, TraitementEtat e) {
		return directoryPhaseRoot(rootDirectory, env, t) + DIRECTORY_TOKEN + e.toString();
	}

	public static String directoryPhaseEtatOK(String rootDirectory, String env, TraitementPhase t) {
		return directoryPhaseEtat(rootDirectory, env, t, TraitementEtat.OK);
	}

	public static String directoryPhaseEtatKO(String rootDirectory, String env, TraitementPhase t) {
		return directoryPhaseEtat(rootDirectory, env, t, TraitementEtat.KO);
	}

	public static String directoryPhaseEtatEnCours(String rootDirectory, String env, TraitementPhase t) {
		return directoryPhaseEtat(rootDirectory, env, t, TraitementEtat.ENCOURS);
	}

	/**
	 * Creation de la table de travail contenant les données en entrée d'une phase et pour un fichier donné
	 * La table en sortie est temporaire ou unlogged car elle est volatile et utilisée que durant l'execution de la phase
	 * La table en entrée est dans le ca d'utilisation principale la table résultat des données 
	 * en sortie la phase précédente pour le fichier donnée.
	 * @param extraColumns
	 * @param tableIn	la table des données en entrée de la phase
	 * @param tableOut	la table des données du fichier en sortie
	 * @param tablePilTemp	la table de pilotage relative à la phase; c'est la liste des fichiers selectionnés pour la phase
	 * @param idSource	le nom du fichier
	 * @param isIdSource	le nom du fichier est-il spécifié ?
	 * @param etatTraitement	l'état du traitement  si on souhaite crée une table en sortie relative à un état particulier
	 * @return
	 */
	public String createTableTravail(String extraColumns, String tableIn, String tableOut, String tablePilTemp
			, String... etatTraitement) {
		StringBuilder requete = new StringBuilder();

		requete.append("\n DROP TABLE IF EXISTS " + tableOut + " CASCADE; \n");

		requete.append("\n CREATE ");
		if (!tableOut.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append("UNLOGGED ");
		}

		requete.append(
				"TABLE " + tableOut + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ");
		requete.append("( ");
		requete.append("\n    SELECT * " + extraColumns);
		requete.append("\n    FROM " + tableIn + " stk ");
		requete.append("\n    WHERE exists ( SELECT 1  ");
		requete.append("\n            FROM " + tablePilTemp + " pil  ");
		requete.append("\n  where pil."+ColumnEnum.ID_SOURCE.getColumnName()+"=stk."+ColumnEnum.ID_SOURCE.getColumnName()+" ");
		if (etatTraitement.length > 0) {
			requete.append(" AND '" + etatTraitement[0] + "'=ANY(pil.etat_traitement) ");
		}
		requete.append(" ) ");
		requete.append(");\n");

		return requete.toString();
	}

	/**
	 * 
	 * @param connexion
	 * @param tableIn
	 * @param tableIdSource
	 * @return
	 */
	public String createTableInherit(String tableIn, String tableIdSource) {
		StaticLoggerDispatcher.info("** createTableOK ** : " + tableIdSource, LOGGER_APISERVICE);

		// si la table in n'est pas vide
		StringBuilder queryToTest = new StringBuilder();
		queryToTest.append("SELECT count(*)>0 FROM (SELECT 1 FROM " + tableIn + " LIMIT 1) u");

		StringBuilder queryToExecute = new StringBuilder();

		// on créé la table héritée que si la table a des enregistrements
		queryToExecute.append("DROP TABLE IF EXISTS " + tableIdSource + ";");
		queryToExecute.append("CREATE TABLE " + tableIdSource + " " + FormatSQL.WITH_NO_VACUUM + " AS SELECT * FROM "
				+ tableIn + ";");

		return FormatSQL.executeIf(queryToTest, queryToExecute);
	}

	/**
	 * Generate the filename
	 * 
	 * @param tableName
	 * @param idSource
	 * @return
	 */
	public static String tableOfIdSource(String tableName, String idSource) {
		String hashText = "";
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA1");
			m.update(idSource.getBytes(), 0, idSource.length());
			hashText = String.format("%1$032x", new BigInteger(1, m.digest()));
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		return tableName + "_" + CHILD_TABLE_TOKEN + "_" + hashText;
	}

	/**
	 * Créer la copie d'une table selectionnée sur un id_source particulier
	 * 
	 * @param TableIn
	 * @param TableOut
	 * @param idSource
	 * @return
	 */
	public String createTablePilotageIdSource(String tableIn, String tableOut, String idSource) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n CREATE ");
		if (!tableOut.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append("UNLOGGED ");
		}
		requete.append(
				"TABLE " + tableOut + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ");
		requete.append("\n SELECT * FROM " + tableIn + " ");
		requete.append("\n WHERE "+ColumnEnum.ID_SOURCE.getColumnName()+" ='" + idSource + "' ");
		requete.append("\n AND etape = 1 ");
		requete.append("\n ; ");
		return requete.toString();
	}

	public String cleanThread() {
		return "DISCARD SEQUENCES; DISCARD TEMP;";
	}

	/**
	 * Met à jour le comptage du nombre d'enregistrement par fichier; nos fichiers
	 * de blocs XML sont devenus tous plats :)
	 * 
	 * @throws ArcException
	 */
	public String updateNbEnr(String tablePilTemp, String tableTravailTemp, String... jointure) throws ArcException {
		StringBuilder query = new StringBuilder();

		// mise à jour du nombre d'enregistrement et du type composite
		StaticLoggerDispatcher.info("** updateNbEnr **", LOGGER_APISERVICE);
		query.append("\n UPDATE " + tablePilTemp + " a ");
		query.append("\n \t SET nb_enr=(select count(*) from " + tableTravailTemp + ") ");

		if (jointure.length > 0) {
			query.append(", jointure= " + FormatSQL.textToSql(jointure[0]) + "");
		}
		query.append(";");

		return query.toString();
	}

	public String createTableTravailIdSource(String tableIn, String tableOut, String idSource, String... extraCols) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n CREATE ");
		if (!tableOut.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append("UNLOGGED ");
		}
		requete.append(
				"TABLE " + tableOut + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ");

		requete.append("\n SELECT * ");

		if (extraCols.length > 0) {
			requete.append(", " + extraCols[0]);
		}

		requete.append("\n FROM " + tableOfIdSource(tableIn, idSource) + "; ");

		return requete.toString();
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
				} catch (Exception ex) {
					loggerDispatcher.error("Erreur dans " + this.getCurrentPhase() + ". ", ex, LOGGER_APISERVICE);
					try {
						this.repriseSurErreur(this.connexion, this.getCurrentPhase(), this.getTablePil(), ex,
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

	/**
	 * Retour arriere vers une phase
	 * 
	 * @param phaseAExecuter
	 * @param env
	 * @param rootDirectory
	 * @param undoFilesSelection
	 */
	public static void backToTargetPhase(TraitementPhase phaseAExecuter, String env, String rootDirectory,
			ArcPreparedStatementBuilder undoFilesSelection) {
		if (phaseAExecuter.getOrdre() == TraitementPhase.INITIALISATION.getOrdre()) {
			resetBAS(env, rootDirectory);
		} else {
			ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
					IHM_SCHEMA, env, rootDirectory, TraitementPhase.INITIALISATION.getNbLigneATraiter());
			try {
				serv.retourPhasePrecedente(phaseAExecuter, undoFilesSelection,
						new ArrayList<>(Arrays.asList(TraitementEtat.OK, TraitementEtat.KO)));
			} finally {
				serv.finaliser();
			}
		}
	}

	/**
	 * reset data in the sandbox
	 * 
	 * @param model
	 * @param env
	 * @param rootDirectory
	 */
	public static void resetBAS(String env, String rootDirectory) {
		try {
			ApiInitialisationService.clearPilotageAndDirectories(rootDirectory, env);
		} catch (Exception e) {
			StaticLoggerDispatcher.info(e, LOGGER_APISERVICE);
		}
		ApiInitialisationService service = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(),
				IHM_SCHEMA, env, rootDirectory, TraitementPhase.INITIALISATION.getNbLigneATraiter());
		try {
			service.resetEnvironnement();
		} finally {
			service.finaliser();
		}
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
	private void repriseSurErreur(Connection connexion, String phase, String tablePil, Exception exception,
			String... tableDrop) throws ArcException {
		// nettoyage de la connexion
		// comme on arrive ici à cause d'une erreur, la base de donnée attend une fin de
		// la transaction
		// si on lui renvoie une requete SQL, il la refuse avec le message
		// ERROR: current transaction is aborted, commands ignored until end of
		// transaction block
		try {
			this.connexion.setAutoCommit(false);
			this.connexion.rollback();
		} catch (SQLException e) {
			throw new ArcException("Error in database connection rollback",e);
		}
		StringBuilder requete = new StringBuilder();

		for (int i = 0; i < tableDrop.length; i++) {
			requete.append("DROP TABLE IF EXISTS " + tableDrop[i] + ";");
		}

		requete.append("WITH t0 AS ( ");
		requete.append(updatePilotageErrorQuery(phase, tablePil, exception));
		requete.append("\n RETURNING "+ColumnEnum.ID_SOURCE.getColumnName()+") ");

		requete.append(resetPreviousPhaseMark(tablePil, null, "t0"));

		UtilitaireDao.get(poolName).executeBlock(connexion, requete);
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
			Exception exception, String... tableDrop) throws ArcException {
		// nettoyage de la connexion
		// comme on arrive ici à cause d'une erreur, la base de donnée attend une fin de
		// la transaction
		// si on lui renvoie une requete SQL, il la refuse avec le message
		// ERROR: current transaction is aborted, commands ignored until end of
		// transaction block
		try {
			this.connexion.setAutoCommit(false);
			this.connexion.rollback();
		} catch (SQLException e) {
			throw new ArcException("Error in database connection rollback",e);
		}

		// promote the application user account to full right
		UtilitaireDao.get("arc").executeImmediate(connexion, switchToFullRightRole());

		StringBuilder requete = new StringBuilder();

		for (int i = 0; i < tableDrop.length; i++) {
			requete.append("DROP TABLE IF EXISTS " + tableDrop[i] + ";");
		}
		requete.append(updatePilotageErrorQuery(phase, tablePil, exception));

		requete.append("\n AND "+ColumnEnum.ID_SOURCE.getColumnName()+" = '" + idSource + "' ");
		requete.append("\n ;");

		requete.append(resetPreviousPhaseMark(tablePil, idSource, null));

		UtilitaireDao.get(poolName).executeBlock(connexion, requete);
	}

	/**
	 * Query to update pilotage table when error occurs
	 * 
	 * @param phase
	 * @param tablePil
	 * @param exception
	 * @return
	 */
	private static StringBuilder updatePilotageErrorQuery(String phase, String tablePil, Exception exception) {
		StringBuilder requete = new StringBuilder();
		requete.append("UPDATE " + tablePil + " SET etape=2, etat_traitement= '{" + TraitementEtat.KO + "}', rapport='"
				+ exception.toString().replace("'", "''").replace("\r", "") + "' ");
		requete.append(
				"\n WHERE phase_traitement='" + phase + "' AND etat_traitement='{" + TraitementEtat.ENCOURS + "}' ");
		return requete;
	}

	/**
	 * permet de récupérer un tableau de la forme id_source | id1 , id2, id3 ...
	 * type_comp | comp1,comp2, comp3 ...
	 * 
	 * @return
	 * @throws ArcException
	 */
	protected HashMap<String, ArrayList<String>> recuperationIdSource(String phaseTraiement) throws ArcException {
		
		ArcPreparedStatementBuilder query=new ArcPreparedStatementBuilder();
		query.append("SELECT p."+ColumnEnum.ID_SOURCE.getColumnName()+" ");
		query.append("FROM " + this.getTablePilTemp() + " p ");
		query.append("ORDER BY "+ColumnEnum.ID_SOURCE.getColumnName());
		query.append(";");
		
		HashMap<String, ArrayList<String>> pil = new GenericBean(
				UtilitaireDao.get(poolName)
						.executeRequest(this.connexion, query ))
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

	public Exception getError() {
		return error;
	}

	public Thread getT() {
		return t;
	}

	public Connection getConnexion() {
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

	public String getTableSeuil() {
		return tableSeuil;
	}

	public void setTableSeuil(String tableSeuil) {
		this.tableSeuil = tableSeuil;
	}

	public String getDirectoryRoot() {
		return directoryRoot;
	}

	public void setDirectoryRoot(String directoryRoot) {
		this.directoryRoot = directoryRoot;
	}

	public String getTableFiltrageRegle() {
		return tableFiltrageRegle;
	}

	public void setTableFiltrageRegle(String tableFiltrageRegle) {
		this.tableFiltrageRegle = tableFiltrageRegle;
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

	
	
}
