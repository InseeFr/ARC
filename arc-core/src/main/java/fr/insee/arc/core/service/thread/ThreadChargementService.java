package fr.insee.arc.core.service.thread;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.ArchiveLoader.ArchiveChargerFactory;
import fr.insee.arc.core.ArchiveLoader.FilesInputStreamLoad;
import fr.insee.arc.core.ArchiveLoader.IArchiveFileLoader;
import fr.insee.arc.core.factory.ChargeurFactory;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.service.ApiChargementService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.service.engine.chargeur.IChargeur;
import fr.insee.arc.core.util.ChargementBrutalTable;
import fr.insee.arc.core.util.Norme;
import fr.insee.arc.core.util.RegleChargement;
import fr.insee.arc.core.util.TypeChargement;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.core.util.StaticLoggerDispatcher;


/**
 * Thread qui va permettre le chargement d'un fichier (1 thread = 1 fichier)
 * 
 * @author S4LWO8
 *
 */
public class ThreadChargementService extends ApiChargementService implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(ThreadChargementService.class);
    int indice;
    private Thread t;
    private String container;
    public String validite;

    public File fileChargement;
    public String entrepot;

    /*
     * / On utiliser plusieur input stream car chacun à une utilité. Et en
     * faisaint ainsi, on évite les problèmes liés au IS
     */
    public FilesInputStreamLoad filesInputStreamLoad;

    public Norme normeOk;

    protected String tableChargementPilTemp;

    public ThreadChargementService(Connection connexion, int currentIndice, ApiChargementService aApi) {

	this.error = null;
	this.indice = currentIndice;
	this.setEnvExecution(aApi.getEnvExecution());
	this.idSource = aApi.getListIdsource().get(ID_SOURCE).get(this.indice);
	this.connexion = connexion;
	try {
	    this.connexion.setClientInfo("ApplicationName", "Chargement fichier " + idSource);
	} catch (SQLClientInfoException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	this.tableChargementPilTemp = "chargement_pil_temp";

	this.container = aApi.getListIdsource().get("container").get(this.indice);
	this.tableChargementRegle = aApi.getTableChargementRegle();
	this.tableNorme = aApi.getTableNorme();
	this.tablePilTemp = aApi.getTablePilTemp();
	this.currentPhase = aApi.getCurrentPhase();
	this.setTablePil(aApi.getTablePil());
	this.paramBatch = aApi.getParamBatch();

	this.directoryIn = Paths.get(
			aApi.getDirectoryRoot(),
			this.envExecution.toUpperCase().replace(".", "_"),
			aApi.getPreviousPhase() + "_" + TraitementEtat.OK).toString() + File.separator;

	// Noms des table temporaires utiles au chargement
	// nom court pour les perfs

	// table A de reception de l'ensemble des fichiers avec nom de colonnes
	// courts
	this.setTableTempA("A");

	// table B de reception de l'ensemble des fichiers brutalement
	this.setTableChargementBrutal("B");

	// table de reception de l'ensemble des fichiers avec nom de colonnes
	// longs
	this.tableTempAll = "L";

	// table de sortie des données dans l'application (hors du module)
	this.tableChargementOK = ApiService.globalTableName(envExecution, this.currentPhase,
		TraitementEtat.OK.toString());

	// récupération des différentes normes dans la base
	this.listeNorme = Norme.getNormesBase(this.connexion, this.tableNorme);

    }

    public void start() {
	StaticLoggerDispatcher.debug("Starting ChargementService", LOGGER);
	if (t == null) {
	    t = new Thread(this, indice + "");
	    t.start();
	}
    }

    @Override
    public void run() {
	StaticLoggerDispatcher.info("Chargement des Fichiers", LOGGER);

	try {
	    // nettoyer la connexion
	    UtilitaireDao.get("arc").executeImmediate(this.connexion, "DISCARD TEMP;");

	    UtilitaireDao.get("arc").executeBlock(connexion,
		    createTablePilotageIdSource(this.tablePilTemp, this.tableChargementPilTemp, this.idSource));

	    // Charger les fichiers
	    ArrayList<String> allCols = new ArrayList<String>();
	    HashMap<String, Integer> colData = new HashMap<>();
	    chargementFichiers(allCols, colData);

	    // retirer de table tempTableA les ids marqués en erreur
	    StaticLoggerDispatcher.info("Retirer de table temporaire les fichiers marqués en erreur", LOGGER);
	    clean();

	    // Mettre à jour le nombre d'enregistrement dans la table de
	    // pilotage temporaire
	    StaticLoggerDispatcher.info("Recopie dans les tables Résultats", LOGGER);
	    insertTableOK(this.connexion, this.tableChargementOK, this.idSource);

	    // Nettoyage
	    StringBuilder blocFin = new StringBuilder();
	    blocFin.append(FormatSQL.dropTable(this.getTableTempA()));
	    blocFin.append(FormatSQL.dropTable(this.tableTempAll));
	    blocFin.append(FormatSQL.dropTable(this.getTableChargementBrutal()));
	    blocFin.append("\nDISCARD SEQUENCES; DISCARD TEMP;");
	    UtilitaireDao.get("arc").executeBlock(this.connexion, blocFin);

	} catch (Exception e) {
	    StaticLoggerDispatcher.info("je suis catché", LOGGER);
	    e.printStackTrace();

	    try {

		// En acs d'erreur on met le fichier en KO avec l'erreur obtenu.
		this.repriseSurErreur(this.connexion, this.getCurrentPhase(), this.tablePil, this.idSource, e,
			"aucuneTableADroper");
	    } catch (SQLException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	    }

	    try {
		Thread.sleep(100);
	    } catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }
	}
    }

    /**
     * Retirer de la table des données les fichiers en KO
     *
     * @throws SQLException
     */
    public void clean() throws SQLException {
	StaticLoggerDispatcher.info("** clean **", LOGGER);

	try {

	    // Check if the source loaded in not in a KO state
	    if (UtilitaireDao.get("arc").hasResults(this.connexion,
		    "select id_source from  " + this.tableChargementPilTemp + " where  etat_traitement='{"
			    + TraitementEtat.KO + "}' ")) {
		UtilitaireDao.get("arc").executeBlock(this.connexion, "TRUNCATE TABLE " + this.getTableTempA() + ";");
	    }

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    /**
     * export des données
     * 
     * @param table
     * @throws Exception
     */
    public void export(String table) throws Exception {
   	String repertoire = properties.getBatchParametersDirectory();
	String envDir = this.getEnvExecution().replace(".", "_").toUpperCase();
	String dirOut = repertoire + envDir + File.separator + "EXPORT";
	File f = new File(dirOut);

	if (!f.exists()) {
	    f.mkdir();
	}

	UtilitaireDao.get(poolName).export(this.connexion, table, dirOut, this.getParamBatch());
    }

    /**
     * Méthode pour charger les fichiers
     * 
     * @param aAllCols
     * @param aColData
     * @throws Exception
     */
    public void chargementFichiers(ArrayList<String> aAllCols, HashMap<String, Integer> aColData) throws Exception {

	StaticLoggerDispatcher.info("** chargementFichiers **", LOGGER);

	java.util.Date beginDate = new java.util.Date();

	this.setColData(aColData);
	this.setAllCols(aAllCols);

	this.setRequeteInsert(new StringBuilder());
	this.requeteBilan = new StringBuilder();

	// Traiter les fichiers avec container
	if (container != null) {
	    chargementFichierAvecContainer();
	}

	UtilitaireDao.get("arc").executeBlock(this.connexion, this.requeteBilan);

	this.getRequeteInsert().setLength(0);
	this.requeteBilan.setLength(0);

	java.util.Date endDate = new java.util.Date();
	StaticLoggerDispatcher.info("** Fichier chargé en " + (endDate.getTime() - beginDate.getTime()) + " ms **", LOGGER);

    }

    /**
     * Méthode qui permet de charger les fichiers s'ils disposent d'un container
     * 
     * @throws Exception
     */
    private void chargementFichierAvecContainer() throws Exception {

    try {
	this.fileChargement = new File(this.directoryIn + File.separator + container);
	this.entrepot = ManipString.substringBeforeFirst(container, "_") + "_";
	
	ArchiveChargerFactory archiveChargerFactory = new ArchiveChargerFactory(fileChargement, this.idSource);
	IArchiveFileLoader archiveChargeur=  archiveChargerFactory.getChargeur(container);

	    this.filesInputStreamLoad = archiveChargeur.loadArchive();
	    choixChargeur();
    } 
    catch(Exception e)
    {
    	throw e;
    }
    finally {
	   	this.filesInputStreamLoad.closeAll();
    }
    }

    /**
     * @param entrepot
     * @param currentEntryChargement
     * @param currentEntryNormage
     * @param tmpInxChargement
     * @param tmpInxNormage
     * @throws Exception
     */
    private void choixChargeur() throws Exception {
	StaticLoggerDispatcher.info("** choixChargeur : " + this.idSource + " **", LOGGER);
	// Si on a pas 1 seule norme alors le fichier est en erreur
	ChargementBrutalTable chgrBrtl = new ChargementBrutalTable();
	chgrBrtl.setConnexion(getConnexion());
	chgrBrtl.setListeNorme(listeNorme);

	// Stockage dans des tableaux pour passage par référence
	Norme[] n=new Norme[1];
	String[] v=new String[1];
	
	try {
		chgrBrtl.calculeNormeAndValiditeFichiers(this.idSource, this.filesInputStreamLoad.getTmpInxNormage(),n,v);
	} catch (Exception e) {
		LoggerHelper.error(LOGGER, e);
		throw e;
	} finally {
		majPilotage(this.idSource, n[0], v[0]);
	}
	
	this.normeOk=n[0];
	this.validite=v[0];

	// Quel type de fichier ?

	normeOk = calculerTypeFichier(normeOk);

	ChargeurFactory chargeurFactory = new ChargeurFactory(this, this.idSource);

	IChargeur chargeur = chargeurFactory.getChargeur(this.normeOk.getRegleChargement().getTypeChargement());

	chargeur.charger();
	
    }

    /**
     * Méthode pour savoir quel est le type du fichier et l'envoyer vers le bon
     * chargeur.
     * Définit la règle de chargement (si présente) dans l'objet norme passé en paramètre.
     * 
     * @param norme
     * @return l'objet Norme avec la règle de chargement renseignée
     * @throws SQLException
     * @throws Exception si aucune règle n'est trouvée
     */
    private Norme calculerTypeFichier(Norme norme) throws Exception {
		GenericBean g = new GenericBean(UtilitaireDao.get(poolName).executeRequest(this.getConnexion(),
			"SELECT type_fichier, delimiter, format FROM " + this.getTableChargementRegle() + " WHERE id_norme ='"
				+ norme.getIdNorme() + "';"));
		if (g.mapContent().isEmpty()) {
			throw new Exception("La norme n'a pas de règle de chargement associée.");
		}
		
		norme.setRegleChargement(new RegleChargement(TypeChargement.getEnum(g.content.get(0).get(0)),
				g.content.get(0).get(1), g.content.get(0).get(2)));
	
		return norme;
    }

    /**
     * 
     * @param connexion
     * @param tableName
     * @throws SQLException
     */
    private void insertTableOK(Connection connexion, String tableName, String idSource) throws Exception {

	updateNbEnr(this.tableChargementPilTemp, this.getTableTempA());

	StaticLoggerDispatcher.info("** insertTableOK **", LOGGER);
	java.util.Date beginDate = new java.util.Date();

	String tableIdSource = tableOfIdSource(tableName, this.idSource);

	// Créer la table des données de la table des donénes chargées
	createTableInherit(connexion, getTableTempA(), tableIdSource);

	StringBuilder requete = new StringBuilder();

	if (paramBatch == null) {
	    requete.append("alter table " + tableIdSource + " inherit " + tableName + "_todo;");
	    requete.append("alter table " + tableIdSource + " inherit " + tableName + ";");
	} else {
	    requete.append("alter table " + tableIdSource + " inherit " + tableName + "_todo;");
	}


	requete.append(this.marquageFinal(this.tablePil, this.tableChargementPilTemp));
	UtilitaireDao.get("arc").executeBlock(connexion, requete);

	

	java.util.Date endDate = new java.util.Date();
	StaticLoggerDispatcher.info("** insertTableOK ** temps : " + (endDate.getTime() - beginDate.getTime()) + " ms",
		LOGGER);

    }

    /**
     * On met à jour la table de pilotage
     * 
     * @param idSource
     * @param listeNorme
     * @return
     * @throws SQLException
     */

    private boolean majPilotage(String idSource, Norme normeOk, String validite) throws Exception {
	boolean erreur = false;
	StaticLoggerDispatcher.info("Mettre à jour la table de pilotage", LOGGER);
	java.util.Date beginDate = new java.util.Date();
	StringBuilder bloc3 = new StringBuilder();

	bloc3.append("UPDATE " + this.tableChargementPilTemp + " a \n");
	bloc3.append("SET\n");

	if (normeOk.getIdNorme() == null) {
	    bloc3.append(" id_norme='" + TraitementRapport.NORMAGE_NO_NORME + "' ");
	    bloc3.append(", validite= '" + TraitementRapport.NORMAGE_NO_DATE + "' ");
	    bloc3.append(", periodicite='" + TraitementRapport.NORMAGE_NO_NORME + "' ");
	    bloc3.append(", etat_traitement='{" + TraitementEtat.KO + "}' ");
	} else {

	    bloc3.append(" id_norme='" + normeOk.getIdNorme() + "' \n");
	    bloc3.append(", validite='" + validite + "' \n");
	    bloc3.append(", periodicite='" + normeOk.getPeriodicite() + "' \n");
	    // bloc3.append(", etat_traitement='{" + TraitementEtat.OK + "}'
	    // \n");
	}

	bloc3.append("where id_source='" + idSource + "' AND phase_traitement='" + this.currentPhase + "'; \n");
	UtilitaireDao.get(poolName).executeBlock(this.getConnexion(), bloc3);
	java.util.Date endDate = new java.util.Date();

	StaticLoggerDispatcher.info(
		"Mettre à jour la table de pilotage temps : " + (endDate.getTime() - beginDate.getTime()) + " ms",
		LOGGER);
	return erreur;
    }

    /*
     * Gettes-Setter
     */
    @Override
	public Thread getT() {
	return t;
    }

    public void setT(Thread t) {
	this.t = t;
    }

    @Override
	public Connection getConnexion() {
	return connexion;
    }

    public void setConnexion(Connection connexion) {
	this.connexion = connexion;
    }

    public String getTableChargementPilTemp() {
	return tableChargementPilTemp;
    }

}