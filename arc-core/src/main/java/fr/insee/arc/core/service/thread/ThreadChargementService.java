package fr.insee.arc.core.service.thread;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.ArchiveLoader.ArchiveChargerFactory;
import fr.insee.arc.core.ArchiveLoader.FilesInputStreamLoad;
import fr.insee.arc.core.ArchiveLoader.IArchiveFileLoader;
import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.factory.ChargeurFactory;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.service.ApiChargementService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.service.engine.chargeur.IChargeur;
import fr.insee.arc.core.util.ChargementBrutalTable;
import fr.insee.arc.core.util.Norme;
import fr.insee.arc.core.util.RegleChargement;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.core.util.TypeChargement;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.Sleep;


/**
 * Thread qui va permettre le chargement d'un fichier (1 thread = 1 fichier)
 * 
 * @author S4LWO8
 *
 */
public class ThreadChargementService extends ApiChargementService implements Runnable, ArcThread<ApiChargementService> {
    private static final Logger LOGGER = LogManager.getLogger(ThreadChargementService.class);
    
    private int indice;

    private String container;

    private String tableChargementPilTemp;
    
    private ArcThreadGenericDao arcThreadGenericDao;
    
    public String validite;

    public FilesInputStreamLoad filesInputStreamLoad;

    public Norme normeOk;
    
    
    @Override
    public void configThread(ScalableConnection connexion, int currentIndice, ApiChargementService aApi) {

	this.indice = currentIndice;
	this.setEnvExecution(aApi.getEnvExecution());
	this.idSource = aApi.getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(this.indice);
	this.connexion = connexion;
	this.tableChargementPilTemp = "chargement_pil_temp";

	this.container = aApi.getTabIdSource().get("container").get(this.indice);
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

	// table de sortie des données dans l'application (hors du module)
	this.tableChargementOK = ApiService.globalTableName(envExecution, this.currentPhase,
		TraitementEtat.OK.toString());

	// récupération des différentes normes dans la base
	this.listeNorme = Norme.getNormesBase(this.connexion.getExecutorConnection(), this.tableNorme);

	// thread generic dao
	arcThreadGenericDao=new ArcThreadGenericDao(connexion, tablePil, tablePilTemp, tableChargementPilTemp, tablePrevious, paramBatch, idSource);
	
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
	    // preparer le chargement
		preparation();
		
	    // Charger les fichiers
	    chargementFichiers();

	    // finaliser le chargement
	    finalisation();
	  
	} catch (ArcException e) {
		StaticLoggerDispatcher.error(e,LOGGER);
	    
		try {
		// En cas d'erreur on met le fichier en KO avec l'erreur obtenu.
		this.repriseSurErreur(this.connexion.getExecutorConnection(), this.getCurrentPhase(), this.tablePil, this.idSource, e,
			"aucuneTableADroper");
	    } catch (ArcException e2) {
			StaticLoggerDispatcher.error(e2,LOGGER);
	    }

	    Sleep.sleep(PREVENT_ERROR_SPAM_DELAY);
	}
    }

    
    /** 
     * Prepare the loading phase
     * @throws ArcException
     */
    private void preparation() throws ArcException
    {

    	ArcPreparedStatementBuilder query= arcThreadGenericDao.preparationDefaultDao();
    	UtilitaireDao.get("arc").executeBlock(connexion.getExecutorConnection(),query.getQueryWithParameters());	
    	
	 }
    
    
    /**
	 * finalisation du chargement
	 * @throws ArcException 
	 */
	private void finalisation() throws ArcException
	{
		
		ArcPreparedStatementBuilder query=new ArcPreparedStatementBuilder();
		  
	    // retirer de table tempTableA les ids marqués en erreur
	    query.append(truncateTableIfKO());
	    
	    // mise à jour du nombre d'enregistrement
	    query.append(updateNbEnr(this.tableChargementPilTemp, this.getTableTempA()));	
	    
	    // Mettre à jour le nombre d'enregistrement dans la table de
	    // pilotage temporaire
	    query.append(insertionFinale(this.connexion.getExecutorConnection(), this.tableChargementOK, this.idSource));

	    // mark file as done in the pilotage table
	    arcThreadGenericDao.marquageFinalDefaultDao(query);

	}

	/**
     * Retirer de la table des données les fichiers en KO
     *
     * @throws ArcException
     */
    private String truncateTableIfKO() {
	StaticLoggerDispatcher.info("** clean **", LOGGER);

	StringBuilder queryTest=new StringBuilder();
	queryTest.append("select count(*)>0 from (select "+ColumnEnum.ID_SOURCE.getColumnName()+" from " + this.tableChargementPilTemp + " where etat_traitement='{"
		    + TraitementEtat.KO + "}' limit 1) u ");

	StringBuilder queryToExecute=new StringBuilder();
	
	queryToExecute.append("TRUNCATE TABLE " + this.getTableTempA() + ";");
	
	return FormatSQL.executeIf(queryTest, queryToExecute);
    }

    /**
     * Méthode pour charger les fichiers
     * 
     * @param aAllCols
     * @param aColData
     * @throws ArcException
     */
    private void chargementFichiers() throws ArcException {

	ArrayList<String> aAllCols = new ArrayList<String>();
	HashMap<String, Integer> aColData = new HashMap<>();
    	
    	
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

	UtilitaireDao.get("arc").executeBlock(this.connexion.getExecutorConnection(), this.requeteBilan);

	this.getRequeteInsert().setLength(0);
	this.requeteBilan.setLength(0);

	java.util.Date endDate = new java.util.Date();
	StaticLoggerDispatcher.info("** Fichier chargé en " + (endDate.getTime() - beginDate.getTime()) + " ms **", LOGGER);

    }

    /**
     * Méthode qui permet de charger les fichiers s'ils disposent d'un container
     * 
     * @throws ArcException
     */
    private void chargementFichierAvecContainer() throws ArcException {

    try {	
		    try {
		        File fileChargement;

				fileChargement = new File(this.directoryIn + File.separator + container);
				
				ArchiveChargerFactory archiveChargerFactory = new ArchiveChargerFactory(fileChargement, this.idSource);
				IArchiveFileLoader archiveChargeur=  archiveChargerFactory.getChargeur(container);
			
				    this.filesInputStreamLoad = archiveChargeur.loadArchive();
				    choixChargeur();
			    } 
			    finally {
						this.filesInputStreamLoad.closeAll();
			    }
	    } catch (ArcException | IOException e) {
			throw new ArcException(e);
		}
    }

    /**
     * @param entrepot
     * @param currentEntryChargement
     * @param currentEntryNormage
     * @param tmpInxChargement
     * @param tmpInxNormage
     * @throws ArcException
     */
    private void choixChargeur() throws ArcException {
	StaticLoggerDispatcher.info("** choixChargeur : " + this.idSource + " **", LOGGER);
	// Si on a pas 1 seule norme alors le fichier est en erreur
	ChargementBrutalTable chgrBrtl = new ChargementBrutalTable();
	chgrBrtl.setConnexion(getConnexion().getExecutorConnection());
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
     * @throws ArcException
     * @throws ArcException si aucune règle n'est trouvée
     */
    private Norme calculerTypeFichier(Norme norme) throws ArcException {
    	
    	ArcPreparedStatementBuilder requete=new ArcPreparedStatementBuilder();
    	requete
    		.append("SELECT type_fichier, delimiter, format ")
    		.append(" FROM "+this.getTableChargementRegle())
    		.append(" WHERE id_norme =" + requete.quoteText(norme.getIdNorme()) + ";");

		GenericBean g = new GenericBean(UtilitaireDao.get(poolName).executeRequest(this.getConnexion().getExecutorConnection(), requete));
		if (g.mapContent().isEmpty()) {
			throw new ArcException("La norme n'a pas de règle de chargement associée.");
		}
		
		norme.setRegleChargement(new RegleChargement(TypeChargement.getEnum(g.content.get(0).get(0)),
				g.content.get(0).get(1), g.content.get(0).get(2)));
	
		return norme;
    }

    /**
     * 
     * @param connexion
     * @param tableName
     * @throws ArcException
     */
    private String insertionFinale(Connection connexion, String tableName, String idSource) {
   	StaticLoggerDispatcher.info("** insertTableOK **", LOGGER);

   	StringBuilder query = new StringBuilder();
	String tableIdSource = tableOfIdSource(tableName, this.idSource);

	// promote the application user account to full right
	query.append(switchToFullRightRole());
	
	// Créer la table des données de la table des donénes chargées
	query.append(createTableInherit(getTableTempA(), tableIdSource));

	return query.toString();
    }

    /**
     * On met à jour la table de pilotage
     * 
     * @param idSource
     * @param listeNorme
     * @return
     * @throws ArcException
     */

    private boolean majPilotage(String idSource, Norme normeOk, String validite) throws ArcException {
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
	}

	bloc3.append("where "+ColumnEnum.ID_SOURCE.getColumnName()+"='" + idSource + "' AND phase_traitement='" + this.currentPhase + "'; \n");
	UtilitaireDao.get(poolName).executeBlock(this.getConnexion().getExecutorConnection(), bloc3);
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
	public ScalableConnection getConnexion() {
	return connexion;
    }

    public void setConnexion(ScalableConnection connexion) {
	this.connexion = connexion;
    }

    public String getTableChargementPilTemp() {
	return tableChargementPilTemp;
    }

}