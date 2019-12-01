package fr.insee.arc.core.service.thread;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import fr.insee.arc.core.archive_loader.ArchiveLoaderFactory;
import fr.insee.arc.core.archive_loader.FilesInputStreamLoad;
import fr.insee.arc.core.archive_loader.FilesInputStreamLoadKeys;
import fr.insee.arc.core.archive_loader.IArchiveFileLoader;
import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.core.model.Norme;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.model.TypeTraitementPhase;
import fr.insee.arc.core.service.ApiIdentifyService;
import fr.insee.arc.core.util.ChargementBrutalTable;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.ManipString;

public class ThreadIdentifyService extends AbstractThreadService {
    private static final Logger LOGGER = Logger.getLogger(ThreadIdentifyService.class);

    //
    int threadNumber;

    // the container of the file
    private String container;

    // validity of the file
    public String validite;

    // the file identified
    public File fileToBeIdentified;

    // the business repository of the file
    public String entrepot;

    // Class which contain multiple inpustream
    public FilesInputStreamLoad filesInputStreamLoad;

    // the norme of the file
    public Norme normeOk;

    private String directoryIn;

    private List<Norme> normList;

    public ThreadIdentifyService(int currentIndice, ApiIdentifyService aApi, Connection zeConnection) {
	super(currentIndice, aApi, zeConnection);

	this.container = aApi.getFilesToProcess().get(currentIndice).getContainer();

	this.directoryIn = aApi.getDirectoryRoot() + this.executionEnv.toUpperCase().replace(".", "_") + File.separator
		+ TypeTraitementPhase.REGISTER + "_" + TraitementState.OK + File.separator;

	// Get all normes in database
	this.normList = getAllNorms();
	
	this.bddTable.addTemporaryTable(BddTable.ID_TABLE_TEMP, BddTable.NOM_TABLE_TEMP);
	this.bddTable.addTemporaryTable(BddTable.ID_TABLE_CHARGEMENT_BRUTAL, BddTable.NOM_TABLE_CHARGEMENT_BRUTAL);
	this.bddTable.addTemporaryTable(BddTable.ID_TABLE_TEMP_ALL, BddTable.NOM_TABLE_TEMP_ALL);

    }

    /**
     * Clean the BDD
     * 
     * @throws SQLException
     */
//    private void cleanBdd() throws SQLException {
//	// Nettoyage
//	StringBuilder blocFin = new StringBuilder();
//
//	blocFin.append("\nDISCARD SEQUENCES; DISCARD TEMP;");
//	UtilitaireDao.get("arc").executeBlock(this.connection, blocFin);
//    }

    /**
     * Traitement métier lié à l'identification.
     * 
     * @throws Exception
     */
    private void identifierFichier() throws Exception {
	// Première étape : ouvrir un flux vers le fichier -> appeler le bon chargeur
	openStreamFromFile();

	// Seconde étape : détecter la norme
	normeFinder();

	// Update table pilotage
	majPilotage();
    }

    /**
     * Open stream from the current file
     * 
     * @throws Exception
     */
    public void openStreamFromFile() throws Exception {
	LoggerDispatcher.info("** openStreamFromFile : " + this.idSource + " **", LOGGER);
	this.fileToBeIdentified = new File(this.directoryIn + File.separator + container);
	this.entrepot = ManipString.substringBeforeFirst(container, "_") + "_";

	ArchiveLoaderFactory archiveChargerFactory = new ArchiveLoaderFactory(fileToBeIdentified, this.idSource);
	IArchiveFileLoader archiveChargeur = archiveChargerFactory.getLoader(container);

	this.filesInputStreamLoad = archiveChargeur.loadArchive(new FilesInputStreamLoadKeys[] { //
		FilesInputStreamLoadKeys.IDENTIFICATION//
		});

    }

    /**
     * What is the norme of the file
     * 
     * @throws Exception
     */
    private void normeFinder() throws Exception {
	LoggerDispatcher.info("** normeFinder : " + this.idSource + " **", LOGGER);
	// Si on a pas 1 seule norme alors le fichier est en erreur
	ChargementBrutalTable chgrBrtl = new ChargementBrutalTable();
	chgrBrtl.setConnexion(getConnection());
	chgrBrtl.setListeNorme(normList);
	Norme[] n=new Norme[1];
	String[] v=new String[1];
			
	chgrBrtl.calculeNormeAndValiditeFichiers(this.idSource, this.filesInputStreamLoad.getTmpInxIdentify(),n,v);
	this.normeOk=n[0];
	this.validite=v[0];

    }

    /**
     * On met à jour la table de pilotage
     * 
     * @param idSourceInArchive
     * @param normeList
     * @return
     * @throws SQLException
     */

    private boolean majPilotage() throws Exception {
	boolean erreur = false;
	LoggerDispatcher.info("Mettre à jour la table de pilotage", LOGGER);
	java.util.Date beginDate = new java.util.Date();
	StringBuilder bloc3 = new StringBuilder();
	bloc3.append("UPDATE " + this.getTablePilTempThread() + " a \n");
	bloc3.append("SET\n");

	if (normeOk.getIdNorme() == null) {
	    bloc3.append(" id_norme='" + TraitementRapport.NORMAGE_NO_NORME + "' ");
	    bloc3.append(", validite= '" + TraitementRapport.NORMAGE_NO_DATE + "' ");
	    bloc3.append(", periodicite='" + TraitementRapport.NORMAGE_NO_NORME + "' ");
	    bloc3.append(", etat_traitement='{" + TraitementState.KO + "}' ");
	} else {

	    bloc3.append(" id_norme='" + this.normeOk.getIdNorme() + "' \n");
	    bloc3.append(", validite='" + this.validite + "' \n");
	    bloc3.append(", periodicite='" + this.normeOk.getPeriodicite() + "' \n");
	}

	bloc3.append("where id_source='" + idSource + "' AND phase_traitement='" + this.tokenInputPhaseName + "'; \n");
	UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.getConnection(), bloc3);
	java.util.Date endDate = new java.util.Date();

	LoggerDispatcher.info(
		"Mettre à jour la table de pilotage temps : " + (endDate.getTime() - beginDate.getTime()) + " ms",
		LOGGER);
	return erreur;
    }

    /**
     * 
     * @param connection
     * @param tableName
     * @throws SQLException
     */
    private void marquagePilotageOk() throws Exception {

	StringBuilder requete = new StringBuilder();

	/*
	 * Passage en ok du fichier. FIXME : gestion des erreurs
	 */
	requete.append(pilotageMarkIdsource(this.getTablePilTempThread(), this.idSource, this.tokenInputPhaseName,
		TraitementState.OK.toString(), null));

	UtilitaireDao.get("arc").executeBlock(this.connection, requete);

    }

    @Override
    public void initialisationTodo() throws Exception {

	UtilitaireDao.get("arc").executeImmediate(this.connection, "DISCARD TEMP;");
	UtilitaireDao.get("arc").executeBlock(connection,
		getRequestTocreateTablePilotageIdSource(this.getTablePilTemp(), this.getTablePilTempThread(), this.idSource));

    }

    @Override
    public void process() throws Exception {
	// Identifier les fichiers
	identifierFichier();

	marquagePilotageOk();

    }

    @Override
    public void finalizePhase() throws SQLException {
//	cleanBdd();

    }

    @Override
    public boolean initialize() {
	// TODO Auto-generated method stub
	return false;
    }

}
