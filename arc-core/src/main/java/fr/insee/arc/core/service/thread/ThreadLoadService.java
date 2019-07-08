package fr.insee.arc.core.service.thread;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import fr.insee.arc.core.archive_loader.ArchiveLoaderFactory;
import fr.insee.arc.core.archive_loader.FilesInputStreamLoad;
import fr.insee.arc.core.archive_loader.FilesInputStreamLoadKeys;
import fr.insee.arc.core.archive_loader.IArchiveFileLoader;
import fr.insee.arc.core.dao.NormeDAO;
import fr.insee.arc.core.factory.ChargeurFactory;
import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.core.model.Norme;
import fr.insee.arc.core.model.PilotageEntity;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.model.TypeTraitementPhase;
import fr.insee.arc.core.service.ApiLoadService;
import fr.insee.arc.core.service.chargeur.ILoader;
import fr.insee.arc.core.util.RegleChargement;
import fr.insee.arc.core.util.TypeChargement;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.queryhandler.BatchQueryHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.SQLExecutor;

/**
 * This is a loading business logic. This thread like other extends {@link AbstractThreadService}.
 * In the previous phase ({@link ThreadIdentifyService}), the app detect the norme of the file.
 * With it it can know how to load the file in database. Each thraed handle only one file.
 * 
 * See {@link ILoader} for more info one the loading of file
 *  
 * @author S4LWO8
 *
 */
public class ThreadLoadService extends AbstractThreadService  {
    private static final Logger LOGGER = Logger.getLogger(ThreadLoadService.class);
    
    // The file to load
    private File fileChargement;
    
    private PilotageEntity fileToLoad;
    
    //The container of the file
    
    //The validity of the file
    private String validity;

    private String entrepot;
    



    private Norme fileNorme;
    private String directoryIn;

    // Noms des table temporaires utiles au chargement
    // nom court pour les perfs
    // table A de reception de l'ensemble des fichiers avec nom de colonnes
    // courts
    private String tableTempA = "A";
    // table B de reception de l'ensemble des fichiers brutalement

    // table de reception de l'ensemble des fichiers avec nom de colonnes
    // longs
    private String tableTempAll = "L";
    private String tableChargementOK;

    
    private StringBuilder requeteInsert = new StringBuilder();
    protected StringBuilder requeteBilan = new StringBuilder();
    private String periodicite;

    private static final String RECEPTION = TypeTraitementPhase.REGISTER.name();

    public ThreadLoadService( int currentIndice, ApiLoadService aApi, Connection connexion) throws Exception {
	super(currentIndice, aApi, connexion);

	this.fileToLoad = aApi.getFilesToProcess().get(currentIndice);


	this.directoryIn = aApi.getDirectoryRoot() + this.executionEnv.toUpperCase().replace(".", "_") + File.separator
		+ RECEPTION + "_" + TraitementState.OK + File.separator;

	// table de sortie des données dans l'application (hors du module)
	this.tableChargementOK = globalTableName(executionEnv, this.tokenInputPhaseName,
		TraitementState.OK.toString());
		
	this.fileNorme = new NormeDAO(new BatchQueryHandler(this.getConnection()), getBddTable().getContextName(BddTable.ID_TABLE_NORME_SPECIFIC)).getFromID(aApi.getFilesToProcess().get(currentIndice).getIdNorme());
		new Norme(aApi.getFilesToProcess().get(currentIndice).getIdNorme());
	this.validity = aApi.getFilesToProcess().get(currentIndice).getValidite();
	this.periodicite = aApi.getFilesToProcess().get(currentIndice).getPeriodicite();

	
	this.fileChargement = new File(this.directoryIn + File.separator + fileToLoad.getContainer());
	this.setEntrepot(ManipString.substringBeforeFirst(fileToLoad.getContainer(), "_") + "_");


    }


    @Override
    public  void initialisationTodo() throws SQLException {
	// nettoyer la connexion
	UtilitaireDao.get("arc").executeImmediate(this.connection, "DISCARD TEMP;");

	UtilitaireDao.get("arc").executeBlock(this.connection,
		getRequestTocreateTablePilotageIdSource(getTablePilTemp(), this.getTablePilTempThread(), this.idSource));
    }

    @Override
    public void process() throws Exception {
	LoggerDispatcher.info("Chargement des Fichiers", LOGGER);

	    // Load file
	    chargementFichiers();

	    clean();

	    insertTableOK(this.connection, this.tableChargementOK, this.idSource);



    }

    @Override
    public void finalizePhase() throws Exception {
	// Nettoyage
	StringBuilder blocFin = new StringBuilder();
	blocFin.append(FormatSQL.dropTable(this.tableTempA));
	blocFin.append(FormatSQL.dropTable(this.tableTempAll));
	blocFin.append("\nDISCARD SEQUENCES; DISCARD TEMP;");
	UtilitaireDao.get("arc").executeBlock(this.connection, blocFin);

    }

    /**
     * Delete ko files data
     *
     * @throws SQLException
     */
    public void clean() throws SQLException {
	LoggerDispatcher.info("** clean **", LOGGER);

	try {

	    // Check if the source loaded in not in a KO state
	    if (UtilitaireDao.get("arc").hasResults(this.connection,
		    "select id_source from  " + this.getTablePilTempThread() + " where  etat_traitement='{"
			    + TraitementState.KO + "}' and id_source in (select id_source from " + this.tableTempA
			    + " limit 1) ")) {
		UtilitaireDao.get("arc").executeBlock(this.connection, "TRUNCATE TABLE " + this.tableTempA + ";");
	    }

	} catch (Exception e) {
	   LoggerHelper.error(LOGGER, "Error when cleaning");
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
	String envDir = this.getExecutionEnv().replace(".", "_").toUpperCase();
	String dirOut = repertoire + envDir + File.separator + "EXPORT";
	File f = new File(dirOut);

	if (!f.exists()) {
	    f.mkdir();
	}

	UtilitaireDao.get(DbConstant.POOL_NAME).export(this.connection, table, dirOut, this.getParamBatch());
    }

    /**
     * Méthode pour charger les fichiers
     * 
     * @param aAllCols
     * @param aColData
     * @throws Exception
     */
    public void chargementFichiers() throws Exception {

	LoggerDispatcher.info("** chargementFichiers **", LOGGER);

	java.util.Date beginDate = new java.util.Date();

	// Traiter les fichiers avec container
	chargementFichierAvecContainer();

	UtilitaireDao.get("arc").executeBlock(this.connection, this.requeteBilan);

	this.requeteInsert.setLength(0);
	this.requeteBilan.setLength(0);

	java.util.Date endDate = new java.util.Date();
	LoggerDispatcher.info("** Fichier chargé en " + (endDate.getTime() - beginDate.getTime()) + " ms **", LOGGER);

    }

    /**
     * Méthode qui permet de charger les fichiers s'ils disposent d'un container
     * 
     * @throws Exception
     */
    private void chargementFichierAvecContainer() throws Exception {

	ArchiveLoaderFactory archiveChargerFactory = new ArchiveLoaderFactory(fileChargement, this.idSource);
	IArchiveFileLoader archiveChargeur = archiveChargerFactory.getLoader(fileChargement.getName());

	FilesInputStreamLoad filesInputStreamLoad = archiveChargeur.loadArchive(//
		new FilesInputStreamLoadKeys[] { //
			FilesInputStreamLoadKeys.LOAD//
			, FilesInputStreamLoadKeys.CSV//
		});
	// Quel type de fichier ?
	setNormeFile(calculerTypeFichier(getNormeFile().getIdNorme()));

	ChargeurFactory chargeurFactory = new ChargeurFactory(this, filesInputStreamLoad);

	ILoader chargeur = chargeurFactory.getChargeur(this.fileNorme.getRegleChargement().getTypeChargement());

	chargeur.charger();
	filesInputStreamLoad.closeAll();

    }

    /**
     * méthode pour savoir quel est le type du fichier et l'envoyer vers le bon
     * chargeur
     * 
     * @param norme
     * @return le type du fichier
     * @throws SQLException
     */
    @SQLExecutor
    private Norme calculerTypeFichier(String idNorme) throws Exception {
	Norme outputed = new Norme();
	GenericBean g = new GenericBean(UtilitaireDao.get(DbConstant.POOL_NAME).executeRequest(this.getConnection(),
		"SELECT type_fichier, delimiter, format FROM " + this.bddTable.getQualifedName(BddTable.ID_TABLE_CHARGEMENT_REGLE) + " WHERE id_norme ='"
			+ idNorme + "';"));

	outputed.setIdNorme(idNorme);
	outputed.setRegleChargement(new RegleChargement(TypeChargement.getEnum(g.content.get(0).get(0)),
		g.content.get(0).get(1), g.content.get(0).get(2)));
	outputed.setPeriodicite(this.periodicite);
	return outputed;
    }

    /**
     * 
     * @param connexion
     * @param tableName
     * @throws SQLException
     */
    private void insertTableOK(Connection connexion, String tableName, String idSource) throws Exception {

	updateNbEnr(this.getTablePilTempThread(), this.tableTempA);

	LoggerDispatcher.info("** insertTableOK **", LOGGER);
	java.util.Date beginDate = new java.util.Date();

	String tableIdSource = tableOfIdSource(tableName, this.idSource);

	// Créer la table des données de la table des donénes chargées
	createTableInherit( this.tableTempA, tableIdSource);

	StringBuilder requete = new StringBuilder();

	if (paramBatch == null) {
	    requete.append("alter table " + tableIdSource + " inherit " + tableName + "_todo;");
	    requete.append("alter table " + tableIdSource + " inherit " + tableName + ";");
	} else {
	    requete.append("alter table " + tableIdSource + " inherit " + tableName + "_todo;");
	}

	requete.append(this.marquageFinal(getTablePil(), this.getTablePilTempThread()));
	UtilitaireDao.get("arc").executeBlock(connexion, requete);

	java.util.Date endDate = new java.util.Date();
	LoggerDispatcher.info("** insertTableOK ** temps : " + (endDate.getTime() - beginDate.getTime()) + " ms",
		LOGGER);

    }


    public String getPeriodicite() {
	return periodicite;
    }


    public void setPeriodicite(String periodicite) {
	this.periodicite = periodicite;
    }


    @Override
    public boolean initialize() {
	// TODO Auto-generated method stub
	return false;
    }


    public String getValidite() {
	return validity;
    }


    public void setValidite(String validite) {
	this.validity = validite;
    }


    public String getEntrepot() {
	return entrepot;
    }


    public void setEntrepot(String entrepot) {
	this.entrepot = entrepot;
    }


    public Norme getNormeFile() {
	return fileNorme;
    }


    public void setNormeFile(Norme fileNorme) {
	this.fileNorme = fileNorme;
    }



}