package fr.insee.arc.core.service.p2chargement.thread;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.service.global.dao.DatabaseConnexionConfiguration;
import fr.insee.arc.core.service.global.dao.HashFileNameConversion;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.core.service.global.dao.RulesOperations;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.core.service.global.dao.TableOperations;
import fr.insee.arc.core.service.global.dao.ThreadOperations;
import fr.insee.arc.core.service.global.scalability.ScalableConnection;
import fr.insee.arc.core.service.global.thread.IThread;
import fr.insee.arc.core.service.p2chargement.ApiChargementService;
import fr.insee.arc.core.service.p2chargement.archiveloader.ArchiveChargerFactory;
import fr.insee.arc.core.service.p2chargement.archiveloader.FilesInputStreamLoad;
import fr.insee.arc.core.service.p2chargement.archiveloader.IArchiveFileLoader;
import fr.insee.arc.core.service.p2chargement.bo.FileIdCard;
import fr.insee.arc.core.service.p2chargement.bo.NormeRules;
import fr.insee.arc.core.service.p2chargement.bo.IdCardChargement;
import fr.insee.arc.core.service.p2chargement.engine.ChargementBrut;
import fr.insee.arc.core.service.p2chargement.engine.IChargeur;
import fr.insee.arc.core.service.p2chargement.factory.ChargeurFactory;
import fr.insee.arc.core.service.p2chargement.factory.TypeChargement;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
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
public class ThreadChargementService extends ApiChargementService implements Runnable, IThread<ApiChargementService> {
	private static final Logger LOGGER = LogManager.getLogger(ThreadChargementService.class);

	private Thread t;

	private String container;

	private String tableChargementPilTemp;

	private ThreadOperations arcThreadGenericDao;

	public FilesInputStreamLoad filesInputStreamLoad;

	public FileIdCard fileIdCard;

	protected String tableChargementOK;

	private String tableTempA;


	@Override
	public void configThread(ScalableConnection connexion, int currentIndice, ApiChargementService aApi) {

		this.envExecution = aApi.getEnvExecution();
		this.idSource = aApi.getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(currentIndice);
		this.connexion = connexion;
		this.container = aApi.getTabIdSource().get("container").get(currentIndice);
		this.tablePilTemp = aApi.getTablePilTemp();
		this.currentPhase = aApi.getCurrentPhase();
		this.tablePil = aApi.getTablePil();
		this.paramBatch = aApi.getParamBatch();
		this.directoryIn = aApi.getDirectoryIn();
		this.listeNorme = aApi.getListeNorme();

		// Noms des tables temporaires utiles au chargement
		// nom court pour les perfs

		// table A de reception de l'ensemble des fichiers avec nom de colonnes
		// courts
		this.tableTempA = "A";
		this.tableChargementPilTemp = "chargement_pil_temp";

		// table de sortie des données dans l'application (hors du module)
		this.tableChargementOK = TableNaming.phaseDataTableName(envExecution, this.currentPhase,
				TraitementEtat.OK);

		// thread generic dao
		arcThreadGenericDao = new ThreadOperations(connexion, tablePil, tablePilTemp, tableChargementPilTemp,
				tablePrevious, paramBatch, idSource);

	}

	public void start() {
		StaticLoggerDispatcher.debug(LOGGER, "Starting ThreadChargementService");
		this.t = new Thread(this);
		t.start();
	}

	@Override
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
				PilotageOperations.traitementSurErreur(this.connexion.getCoordinatorConnection(), this.getCurrentPhase(), this.tablePil,
						this.idSource, processException);
			} catch (ArcException marquageException) {
				marquageException.logFullException();
			}

			Sleep.sleep(PREVENT_ERROR_SPAM_DELAY);
		}
	}

	/**
	 * Prepare the loading phase
	 * 
	 * @throws ArcException
	 */
	private void preparation() throws ArcException {

		ArcPreparedStatementBuilder query = arcThreadGenericDao.preparationDefaultDao();
		UtilitaireDao.get(0).executeBlock(connexion.getExecutorConnection(), query.getQueryWithParameters());

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
				this.filesInputStreamLoad.closeAll();
			}
		} catch (IOException e) {
			throw new ArcException(e, ArcExceptionMessage.FILE_CLOSE_FAILED, fileChargement.getAbsolutePath());
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
	private IChargeur chooseLoader() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "** choixChargeur : " + this.idSource + " **");
		// Si on a pas 1 seule norme alors le fichier est en erreur
		ChargementBrut chgrBrtl = new ChargementBrut();
		chgrBrtl.setConnexion(getConnexion().getExecutorConnection());
		chgrBrtl.setListeNorme(listeNorme);		
		// Stockage dans des tableaux pour passage par référence
		
		this.fileIdCard = new FileIdCard(this.idSource);
		
		try {
			chgrBrtl.calculeNormeAndValiditeFichiers(this.filesInputStreamLoad.getTmpInxNormage(), this.fileIdCard);
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, e);
			throw e;
		} finally {
			majPilotage(this.idSource, this.fileIdCard);
		}

		// Quel type de fichier ?

		calculerTypeFichier();

		ChargeurFactory chargeurFactory = new ChargeurFactory(this);

		return chargeurFactory.getChargeur(this.fileIdCard.getIdCardChargement().getTypeChargement());
	}

	/**
	 * Méthode pour savoir quel est le type du fichier et l'envoyer vers le bon
	 * chargeur. Définit la règle de chargement (si présente) dans l'objet norme
	 * passé en paramètre.
	 * 
	 * @param norme
	 * @return l'objet Norme avec la règle de chargement renseignée
	 * @throws ArcException
	 * @throws ArcException si aucune règle n'est trouvée
	 */
	private void calculerTypeFichier() throws ArcException {
		Map<String, List<String>> regle = RulesOperations.getBean(this.getConnexion().getExecutorConnection(),
				RulesOperations.getRegles(ViewEnum.CHARGEMENT_REGLE.getFullName(this.envExecution), this.fileIdCard));
		
		if (regle.get(ColumnEnum.TYPE_FICHIER.getColumnName()).isEmpty())
		{
			throw new ArcException(ArcExceptionMessage.LOAD_RULES_NOT_FOUND, fileIdCard.getIdNorme());
		}
		
		if (regle.get(ColumnEnum.TYPE_FICHIER.getColumnName()).size()>1)
		{
			throw new ArcException(ArcExceptionMessage.LOAD_RULES_NOT_FOUND, fileIdCard.getIdNorme());
		}
		
		fileIdCard.setIdCardChargement(new IdCardChargement(
				TypeChargement.getEnum(regle.get(ColumnEnum.TYPE_FICHIER.getColumnName()).get(0)),
				regle.get(ColumnEnum.DELIMITER.getColumnName()).get(0)
				, regle.get(ColumnEnum.FORMAT.getColumnName()).get(0)));
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

	private boolean majPilotage(String idSource, FileIdCard fileIdCard) throws ArcException {
		boolean erreur = false;
		StaticLoggerDispatcher.info(LOGGER, "Mettre à jour la table de pilotage");
		java.util.Date beginDate = new java.util.Date();
		StringBuilder bloc3 = new StringBuilder();

		bloc3.append("UPDATE " + this.tableChargementPilTemp + " a \n");
		bloc3.append("SET\n");

		if (fileIdCard.getIdNorme() == null) {
			bloc3.append(" id_norme='" + TraitementRapport.NORMAGE_NO_NORME + "' ");
			bloc3.append(", validite= '" + TraitementRapport.NORMAGE_NO_DATE + "' ");
			bloc3.append(", periodicite='" + TraitementRapport.NORMAGE_NO_NORME + "' ");
			bloc3.append(", etat_traitement='{" + TraitementEtat.KO + "}' ");
		} else {

			bloc3.append(" id_norme='" + fileIdCard.getIdNorme() + "' \n");
			bloc3.append(", validite='" + fileIdCard.getValidite() + "' \n");
			bloc3.append(", periodicite='" + fileIdCard.getPeriodicite() + "' \n");
		}

		bloc3.append("where " + ColumnEnum.ID_SOURCE.getColumnName() + "='" + idSource + "' AND phase_traitement='"
				+ this.currentPhase + "'; \n");
		UtilitaireDao.get(0).executeBlock(this.getConnexion().getExecutorConnection(), bloc3);
		java.util.Date endDate = new java.util.Date();

		StaticLoggerDispatcher.info(
				LOGGER,
				"Mettre à jour la table de pilotage temps : " + (endDate.getTime() - beginDate.getTime()) + " ms");
		return erreur;
	}

	/*
	 * Gettes-Setter
	 */
	@Override
	public Thread getT() {
		return t;
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
	
	

}