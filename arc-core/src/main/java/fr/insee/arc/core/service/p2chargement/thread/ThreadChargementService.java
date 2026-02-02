package fr.insee.arc.core.service.p2chargement.thread;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.dao.DatabaseConnexionConfiguration;
import fr.insee.arc.core.service.global.dao.HashFileNameConversion;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.core.service.global.dao.TableOperations;
import fr.insee.arc.core.service.global.dao.ThreadOperations;
import fr.insee.arc.core.service.global.scalability.ScalableConnection;
import fr.insee.arc.core.service.global.thread.ThreadConstant;
import fr.insee.arc.core.service.global.thread.ThreadTemplate;
import fr.insee.arc.core.service.global.thread.ThreadTemporaryTable;
import fr.insee.arc.core.service.mutiphase.thread.ThreadMultiphaseService;
import fr.insee.arc.core.service.p2chargement.archiveloader.ArchiveChargerFactory;
import fr.insee.arc.core.service.p2chargement.archiveloader.FilesInputStreamLoad;
import fr.insee.arc.core.service.p2chargement.archiveloader.IArchiveFileLoader;
import fr.insee.arc.core.service.p2chargement.bo.IChargeur;
import fr.insee.arc.core.service.p2chargement.factory.ChargeurFactory;
import fr.insee.arc.core.service.p2chargement.operation.ChargementBrut;
import fr.insee.arc.core.service.p2chargement.operation.ChargementRulesOperation;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.security.SqlInjectionChecked;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.Sleep;

/**
 * Thread qui va permettre le chargement d'un fichier (1 thread = 1 fichier)
 * 
 * @author S4LWO8
 *
 */
public class ThreadChargementService extends ThreadTemplate {
	private static final Logger LOGGER = LogManager.getLogger(ThreadChargementService.class);

	private String idSource;
	
	private String container;

	private String tableChargementPilTemp;

	private ThreadOperations arcThreadGenericDao;

	private FilesInputStreamLoad filesInputStreamLoad;

	private FileIdCard fileIdCard;

	protected String tableChargementOK;

	private String tableTempA;
	
	private TraitementPhase currentExecutedPhase = TraitementPhase.CHARGEMENT;
	private TraitementPhase previousExecutedPhase = this.currentExecutedPhase.previousPhase();
	
	
	
	public void configThread(ScalableConnection connexion, int currentIndice, ThreadMultiphaseService aApi, boolean beginNextPhase, boolean cleanPhase) {
  

		this.envExecution = aApi.getEnvExecution();
		this.idSource = aApi.getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(currentIndice);
		this.connexion = connexion;
		this.container = aApi.getTabIdSource().get("container").get(currentIndice);
		this.tablePilTemp = aApi.getTablePilTemp();
		this.tablePil = aApi.getTablePil();
		this.paramBatch = aApi.getParamBatch();
		this.directoryIn = aApi.getDirectoryIn();
		this.listeNorme = aApi.getListeNorme();

		// Noms des tables temporaires utiles au chargement
		// nom court pour les perfs

		// table a de reception de l'ensemble des fichiers avec nom de colonnes
		// courts
		this.tableTempA = ThreadTemporaryTable.TABLE_TEMP_CHARGEMENT_A;
		this.tableChargementPilTemp = ThreadTemporaryTable.TABLE_PILOTAGE_THREAD;

		// table de sortie des données dans l'application (hors du module)
		this.tableChargementOK = TableNaming.phaseDataTableName(envExecution, this.currentExecutedPhase,
				TraitementEtat.OK);
		
		String previousExecutedPhaseTable = TableNaming.phaseDataTableName(this.envExecution, this.previousExecutedPhase, TraitementEtat.OK);

		// thread generic dao
		arcThreadGenericDao = new ThreadOperations(this.currentExecutedPhase, beginNextPhase, cleanPhase, connexion, tablePil, tablePilTemp, tableChargementPilTemp,
				previousExecutedPhaseTable, paramBatch, idSource);

	}
	
	public void run() {
		StaticLoggerDispatcher.info(LOGGER, "Chargement des Fichiers");

		try {
			// preparer le chargement
			preparation();

			// Charger les fichiers
			chargementFichiers();

			// finaliser le chargement
			finalisation();

		} catch (ArcException processException) {
			
			processException.logFullException();

			try {
				// En cas d'erreur on met le fichier en KO avec l'erreur obtenu.
				PilotageOperations.traitementSurErreur(this.connexion.getCoordinatorConnection(), this.currentExecutedPhase, this.tablePil,
						this.idSource, processException);
			} catch (ArcException marquageException) {
				marquageException.logFullException();
			}

			Sleep.sleep(ThreadConstant.PREVENT_ERROR_SPAM_DELAY);
		}
	}

	/**
	 * Prepare the loading phase
	 * 
	 * @throws ArcException
	 */
	private void preparation() throws ArcException {

		ArcPreparedStatementBuilder query = arcThreadGenericDao.preparationDefaultDao();
		UtilitaireDao.get(0).executeRequest(connexion.getExecutorConnection(), query);

	}

	/**
	 * finalisation du chargement
	 * 
	 * @throws ArcException
	 */
	private void finalisation() throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		// retirer de table tempTableA les ids marqués en erreur
		query.append(truncateTableIfKO());

		// mise à jour du nombre d'enregistrement
		query.append(PilotageOperations.queryUpdateNbEnr(this.tableChargementPilTemp, this.getTableTempA()));

		// Créer la table chargement OK
		query.append(insertionFinale(this.tableChargementOK, this.idSource));

		// mark file as done in the pilotage table
		arcThreadGenericDao.marquageFinalDefaultDao(query);

	}

	/**
	 * Retirer de la table des données les fichiers en KO
	 *
	 * @throws ArcException
	 */
	private String truncateTableIfKO() {
		StaticLoggerDispatcher.info(LOGGER, "** clean **");

		StringBuilder queryTest = new StringBuilder();
		queryTest.append("select count(*)>0 from (select " + ColumnEnum.ID_SOURCE.getColumnName() + " from "
				+ this.tableChargementPilTemp + " where etat_traitement='{" + TraitementEtat.KO + "}' limit 1) u ");

		StringBuilder queryToExecute = new StringBuilder();

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

		StaticLoggerDispatcher.info(LOGGER, "** chargementFichiers **");

		java.util.Date beginDate = new java.util.Date();

		// Traiter les fichiers avec container
		if (container != null) {
			chargementFichierAvecContainer();
		}

		java.util.Date endDate = new java.util.Date();
		StaticLoggerDispatcher.info(LOGGER,
				"** Fichier chargé en " + (endDate.getTime() - beginDate.getTime()) + " ms **");

	}

	/**
	 * Méthode qui permet de charger les fichiers s'ils disposent d'un container
	 * 
	 * @throws ArcException
	 */
	private void chargementFichierAvecContainer() throws ArcException {

		File fileChargement = new File(this.directoryIn + File.separator + container);

		try {
			try {

				ArchiveChargerFactory archiveChargerFactory = new ArchiveChargerFactory(fileChargement, this.idSource);
				IArchiveFileLoader archiveChargeur = archiveChargerFactory.getChargeur(container);

				this.filesInputStreamLoad = archiveChargeur.prepareArchiveStreams();
				IChargeur targetLoader = chooseLoader();
				targetLoader.charger();
				
			} finally {
				closeInputStreamLoad(fileChargement);
			}
		} catch (IOException e) {
			throw new ArcException(e, ArcExceptionMessage.FILE_CLOSE_FAILED, fileChargement.getAbsolutePath());
		}
	}
	
	/**
	 * Close the filesInputStreamLoad
	 * @throws ArcException 
	 * Close InputStream that read csv headers
	 * @throws IOException 
	 */
	private void closeInputStreamLoad(File fileChargement) throws ArcException, IOException
	{
		if (filesInputStreamLoad==null) {
			throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, fileChargement.getAbsolutePath());
		}
		this.filesInputStreamLoad.closeAll();
	}

	/**
	 * @param entrepot
	 * @param currentEntryChargement
	 * @param currentEntryNormage
	 * @param tmpInxChargement
	 * @param tmpInxNormage
	 * @throws ArcException
	 */
	private IChargeur chooseLoader() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "** choixChargeur : " + this.idSource + " **");
		// Si on a pas 1 seule norme alors le fichier est en erreur
		ChargementBrut chgrBrtl = new ChargementBrut();
		chgrBrtl.setConnexion(getConnexion().getExecutorConnection());
		chgrBrtl.setListeNorme(listeNorme);		
		// Stockage dans des tableaux pour passage par référence
		
		this.fileIdCard = new FileIdCard(this.idSource);
		
		try {
			chgrBrtl.calculeNormeAndValiditeFichiers(this.filesInputStreamLoad.getTmpInxNormage(), this.fileIdCard, envExecution);
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, e);
			throw e;
		} finally {
			majPilotage(this.idSource, this.fileIdCard);
		}

		// Quel type de fichier ?

		ChargementRulesOperation.fillChargementRules(this.getConnexion().getExecutorConnection(), envExecution, this.fileIdCard);

		ChargeurFactory chargeurFactory = new ChargeurFactory(this);

		return chargeurFactory.getChargeur(this.fileIdCard.getIdCardChargement().getTypeChargement());
	}

	/**
	 * 
	 * @param connexion
	 * @param tableName
	 * @throws ArcException
	 */
	private String insertionFinale(String tableName, String idSource) throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "** insertTableOK **");

		StringBuilder query = new StringBuilder();
		String tableIdSource = HashFileNameConversion.tableOfIdSource(tableName, idSource);

		// promote the application user account to full right
		query.append(DatabaseConnexionConfiguration.switchToFullRightRole());

		// Créer la table des données de la table des donénes chargées
		query.append(TableOperations.createTableInherit(getTableTempA(), tableIdSource));
		
		// dropper les tables temporaires
		query.append(FormatSQL.dropTable(getTableTempA()));

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
	@SqlInjectionChecked
	private boolean majPilotage(String idSource, FileIdCard fileIdCard) throws ArcException {
		boolean erreur = false;
		StaticLoggerDispatcher.info(LOGGER, "Mettre à jour la table de pilotage");
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append("UPDATE " + this.tableChargementPilTemp + " a \n");
		query.append("SET ");

		if (fileIdCard.getIdNorme() == null) {
			query.append("id_norme='" + TraitementRapport.NORMAGE_NO_NORME + "' ");
			query.append(", validite= '" + TraitementRapport.NORMAGE_NO_DATE + "' ");
			query.append(", periodicite='" + TraitementRapport.NORMAGE_NO_NORME + "' ");
			query.append(", etat_traitement='{" + TraitementEtat.KO + "}' ");
		} else {

			query.append("id_norme=").appendText(fileIdCard.getIdNorme());
			query.append(", validite=").appendText(fileIdCard.getValidite());
			query.append(", periodicite=").appendText(fileIdCard.getPeriodicite());
		}

		query.append(" WHERE " + ColumnEnum.ID_SOURCE.getColumnName() + "=").appendText(idSource);
		query.append(" AND phase_traitement='"+ this.currentExecutedPhase + "';\n");
		UtilitaireDao.get(0).executeRequest(this.getConnexion().getExecutorConnection(), query);

		StaticLoggerDispatcher.info(LOGGER,	"Fin mettre à jour la table de pilotage");
		return erreur;
	}

	public String getTableChargementPilTemp() {
		return tableChargementPilTemp;
	}

	public String getTableTempA() {
		return tableTempA;
	}

	public void setTableTempA(String tableTempA) {
		this.tableTempA = tableTempA;
	}

	public FileIdCard getFileIdCard() {
		return fileIdCard;
	}

	public void setFileIdCard(FileIdCard fileIdCard) {
		this.fileIdCard = fileIdCard;
	}

	public FilesInputStreamLoad getFilesInputStreamLoad() {
		return filesInputStreamLoad;
	}

	public void setFilesInputStreamLoad(FilesInputStreamLoad filesInputStreamLoad) {
		this.filesInputStreamLoad = filesInputStreamLoad;
	}


	public TraitementPhase getCurrentExecutedPhase() {
		return currentExecutedPhase;
	}

}