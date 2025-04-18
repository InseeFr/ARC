package fr.insee.arc.batch;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.arc.batch.dao.BatchArcDao;
import fr.insee.arc.batch.threadrunners.PhaseParameterKeys;
import fr.insee.arc.batch.threadrunners.PhaseThreadFactory;
import fr.insee.arc.core.model.BatchMode;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.DatabaseMaintenance;
import fr.insee.arc.core.service.global.util.Patch;
import fr.insee.arc.core.service.kubernetes.ApiManageExecutorDatabase;
import fr.insee.arc.core.service.mutiphase.ApiMultiphaseService;
import fr.insee.arc.core.service.p0initialisation.dbmaintenance.BddPatcher;
import fr.insee.arc.core.service.p0initialisation.filesystem.BuildFileSystem;
import fr.insee.arc.core.service.p0initialisation.metadata.SynchronizeRulesAndMetadataOperation;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.core.service.s3.ArcS3;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.batch.IReturnCode;
import fr.insee.arc.utils.consumer.ThrowingRunnable;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.security.SecurityDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.Sleep;

/**
 * Classe lanceur de l'application Accueil Reception Contrôle 07/08/2015 Version
 * pour les tests de performance et pré-production
 * 
 * @author Manu
 * 
 */
class BatchARC implements IReturnCode {
	private static final Logger LOGGER = LogManager.getLogger(BatchARC.class);
	private static Map<String, String> mapParam = new HashMap<>();

	/**
	 * variable dateInitialisation si vide (ou si date du jour+1 depassé à 20h), je
	 * lance initialisation et j'initialise dateInitialisation à la nouvelle date du
	 * jour puis une fois terminé, je lancent la boucle des batchs si date du jour+1
	 * depassé a 20h, - j'indique aux autre batchs de s'arreter - une fois arretés,
	 * je met tempo à la date du jour - je lance initialisation etc.
	 */

	private @Autowired PropertiesHandler properties;

	// the sandbox schema where batch process runs
	private String envExecution;

	// file directory
	private String repertoire;

	// fréquence à laquelle les phases sont démarrées
	private int poolingDelay;

	// heure d'initalisation en production
	private Integer hourToTriggerInitializationInProduction;

	// interval entre chaque initialisation en nb de jours
	private Integer intervalForInitializationInDay;

	// nombre d'iteration de la boucle batch entre chaque routine de maintenance de
	// la base de données
	private Integer numberOfIterationBewteenDatabaseMaintenanceRoutine;

	// nombre d'iteration de la boucle batch entre chaque routine de vérification du
	// reste à faire
	private Integer numberOfIterationBewteenCheckTodo;

	// temps à attendre pour que les executors soient montés
	private Integer waitExecutorTimerInMS;

	// true = the batch will resume the process from a formerly interrupted batch
	// false = the batch will proceed to a new load
	// Maintenance initialization process can only occur in this case
	private boolean dejaEnCours;

	// Array of phases
	private List<TraitementPhase> phases = new ArrayList<>();
	// Map of thread by phase
	private Map<TraitementPhase, List<PhaseThreadFactory>> pool = new HashMap<>();
	// delay between phase start
	private int delay;

	// loop attribute
	Thread maintenance = new Thread();

	// is production on ?
	private boolean productionOn;

	// data from executors must be automatically exported ?
	private boolean exportOn;

	// will delete executor nods
	private boolean volatileOn;

	private boolean exit = false;
	private int iteration = 0;

	private static void message(String msg) {
		LoggerHelper.warn(LOGGER, msg);
	}

	private BatchArcDao dao;
	
	
	/**
	 * Lanceur MAIN arc
	 * 
	 * @param args
	 */
	void execute() {
		
		try (Connection batchConnection = UtilitaireDao.get(0).getDriverConnexion();)
		{
			
			dao = new BatchArcDao(batchConnection);

			// cache dns ip adress
			batchAvoidDnsSpam();
			
			// patch database
			batchPatchDatabaseAndFileSystem();

			// set batch parameters
			executeIfProductionActive(this::batchParametersGet);

			// prepare batch
			executeIfProductionActive(this::batchEnvironmentPrepare);

			// execute Initialization phase or Executor synchronization
			executeIfProductionActive(this::phaseInitializationExecute);

			// execute Reception phase
			executeIfProductionActive(this::phaseReceptionExecute);

			// execute the loop
			executeIfProductionActive(this::executeLoopOverPhases);

			// finalize batch
			executeIfProductionActive(this::batchFinalize);

		} catch (Exception ex) {
			LoggerHelper.errorGenTextAsComment(BatchARC.class, "main()", LOGGER, ex);
			System.exit(STATUS_FAILURE_TECHNICAL_WARNING);
		}

		// exit code
		endBatch();

	}

	/**
	 * end the batch
	 */
	private void endBatch() {
		message("Fin du batch");
		System.exit(STATUS_SUCCESS);
	}


	/**
	 * Remap given database uri to use ip adress instead of dns name during batch loop
	 */
	private void batchAvoidDnsSpam() {
		properties.setRemapHostAddress(t -> {
			try {
				return InetAddress.getByName(t).getHostAddress();
			} catch (UnknownHostException e) {
				throw new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_FAILED);
			}
		}
		);
	}
	
	
	

	private void executeIfProductionActive(ThrowingRunnable method) throws ArcException {
		if (!isProductionOn()) {
			message("La production est arretée !");
			return;
		}
		method.run();
	}

	/**
	 * if kubernetes executor are defined and volatile is on
	 * 
	 * @param method
	 * @throws ArcException
	 */
	private void executeIfVolatile(ThrowingRunnable method) throws ArcException {
		if (!this.volatileOn) {
			return;
		}

		message("Volatile mode is on");
		method.run();
	}

	private void executeIfParquetActive(ThrowingRunnable method) throws ArcException {
		if (!this.exportOn) {
			return;
		}

		message("Parquet export is on");
		method.run();
	}

	private void executeIfExecutors(ThrowingRunnable method) throws ArcException {
		if (!ArcDatabase.isScaled()) {
			return;
		}
		message("Database is scaled");
		method.run();
	}

	/**
	 * Patch or create the ARC database with tiniotialization script
	 * @throws ArcException 
	 */
	private void batchPatchDatabaseAndFileSystem() throws ArcException {
		
		message("Main");
		message("Batch ARC " + properties.fullVersionInformation().toString());
		
		
		message("Patching database");

		new BddPatcher().bddScript(dao.getBatchConnection());
		
		BDParameters bdParameters = new BDParameters(ArcDatabase.COORDINATOR);

		// either we take env and envExecution from database or properties
		// default is from properties
		if (Boolean.parseBoolean(bdParameters.getString(dao.getBatchConnection(), "LanceurARC.envFromDatabase", "false"))) {
			envExecution = bdParameters.getString(dao.getBatchConnection(), "LanceurARC.envExecution", "arc_prod");
		} else {
			envExecution = properties.getBatchExecutionEnvironment();
		}

		envExecution = Patch.normalizeSchemaName(envExecution);
		
		message("Execution sandbox is "+envExecution);
		
		// security check if envExecution is valid
		message("Patching filesytem");
		envExecution=SecurityDao.validateEnvironnement(envExecution);
		
		new BddPatcher().bddScript(dao.getBatchConnection(), envExecution);
		
		
		// build sandbox filesystem
		new BuildFileSystem(dao.getBatchConnection(), new String[] {this.envExecution}).execute();
		
	}
	
	/**
	 * Get batch parameters
	 */
	private void batchParametersGet() {

		message("Récupération des paramètres du batch");

		BDParameters bdParameters = new BDParameters(ArcDatabase.COORDINATOR);

		boolean keepInDatabase = Boolean
				.parseBoolean(bdParameters.getString(dao.getBatchConnection(), "LanceurARC.keepInDatabase", "false"));

		// pour le batch en cours, l'ensemble des enveloppes traitées ne peut pas
		// excéder une certaine taille
		int tailleMaxReceptionEnMb = bdParameters.getInt(dao.getBatchConnection(), "LanceurARC.tailleMaxReceptionEnMb", 10);

		// Maximum number of files to load
		int maxFilesToLoad = bdParameters.getInt(dao.getBatchConnection(), "LanceurARC.maxFilesToLoad", 101);

		// Maximum number of files processed in each phase iteration
		int maxFilesPerPhase = bdParameters.getInt(dao.getBatchConnection(), "LanceurARC.maxFilesPerPhase", 1000000);

		// fréquence à laquelle les phases sont démarrées
		this.poolingDelay = bdParameters.getInt(dao.getBatchConnection(), "LanceurARC.poolingDelay", 1000);

		// heure d'initalisation en production
		hourToTriggerInitializationInProduction = bdParameters.getInt(dao.getBatchConnection(),
				"ApiService.HEURE_INITIALISATION_PRODUCTION", 22);

		// interval entre chaque initialisation en nb de jours
		intervalForInitializationInDay = bdParameters.getInt(dao.getBatchConnection(), "LanceurARC.INTERVAL_JOUR_INITIALISATION", 7);

		// nombre d'iteration de la boucle batch entre chaque routine de maintenance de
		// la base de données
		numberOfIterationBewteenDatabaseMaintenanceRoutine = bdParameters.getInt(dao.getBatchConnection(),
				"LanceurARC.DATABASE_MAINTENANCE_ROUTINE_INTERVAL", 500);

		// nombre d'iteration de la boucle batch entre chaque routine de vérification du
		// reste à faire
		numberOfIterationBewteenCheckTodo = bdParameters.getInt(dao.getBatchConnection(), "LanceurARC.DATABASE_CHECKTODO_ROUTINE_INTERVAL",
				10);

		// wait executor pods
		waitExecutorTimerInMS = bdParameters.getInt(dao.getBatchConnection(), "LanceurARC.DATABASE_WAIT_FOR_EXECUTORS_IN_MS",
				30000);
		
		
		repertoire = properties.getBatchParametersDirectory();

		mapParam.put(PhaseParameterKeys.KEY_FOR_DIRECTORY_LOCATION, repertoire);
		mapParam.put(PhaseParameterKeys.KEY_FOR_BATCH_MODE, BatchMode.NORMAL);
		mapParam.put(PhaseParameterKeys.KEY_FOR_EXECUTION_ENVIRONMENT, envExecution);
		mapParam.put(PhaseParameterKeys.KEY_FOR_MAX_SIZE_RECEPTION, String.valueOf(tailleMaxReceptionEnMb));
		mapParam.put(PhaseParameterKeys.KEY_FOR_MAX_FILES_TO_LOAD, String.valueOf(maxFilesToLoad));
		mapParam.put(PhaseParameterKeys.KEY_FOR_MAX_FILES_PER_PHASE, String.valueOf(maxFilesPerPhase));
		mapParam.put(PhaseParameterKeys.KEY_FOR_KEEP_IN_DATABASE, String.valueOf(keepInDatabase));

		message(mapParam.toString());

		this.exportOn = !properties.getProcessExport().isEmpty();

		message("Export to parquet : "+exportOn);

		// volatile mode is on if kubernetes have executor defined and volatile mode is
		// set
		this.volatileOn = !properties.getKubernetesExecutorVolatile().isEmpty() //
				&& (properties.getKubernetesExecutorNumber() > 0);

		message("Volatile database : "+volatileOn);

	}

	/**
	 * prepare batch : rollback errors, prepare volatile database, synchronize with
	 * Initialization phase
	 * 
	 * @throws ArcException
	 * @throws IOException
	 */
	private void batchEnvironmentPrepare() throws ArcException {

		message("Préparation de l'environnement");

		// create volatile database
		executeIfVolatile(this::executorsDatabaseCreate);
		
		// recover process if last batch didn't finish well
		resetPendingFilesFromPilotage();

		// database maintenance on pilotage table so that index won't bloat
		maintenanceTablePilotageBatch();

		// on vide les repertoires de chargement OK, KO, ENCOURS
		effacerRepertoireChargement(repertoire, envExecution);

		// des archives n'ont elles pas été traitées jusqu'au bout ?
		deplacerFichiersNonTraites();

	}

	/**
	 * Reset the pending file status from pilotage Pending files can occurs when the
	 * former batch run didn't finish or crahs
	 * 
	 * @throws ArcException
	 */
	private void resetPendingFilesFromPilotage() throws ArcException {

		dao.execQueryResetPendingFilesInPilotageTable(envExecution);

		// if volatile mode on, put back all the not fully proceeded files in reception
		// phase
		executeIfVolatile(() -> dao.execQueryResetPendingFilesInPilotageTableVolatile(envExecution));
	}

	private void executorsDatabaseCreate() throws ArcException {
		message(ApiManageExecutorDatabase.delete().toString());
		message(ApiManageExecutorDatabase.create().toString());
		Sleep.sleep(waitExecutorTimerInMS);
	}
	
	/***
	 * Delete files, export to parquet
	 * 
	 * @throws ArcException
	 */
	private void batchFinalize() throws ArcException {

		// Delete entry files if no interruption or no problems
		effacerRepertoireChargement(repertoire, envExecution);

		executeIfParquetActive(this::exportToParquet);

		message("Traitement Fin");

	}

	/**
	 * Export business mapping tables to parquet Only in volatile mode
	 * @throws ArcException 
	 */
	private void exportToParquet() throws ArcException {
		PhaseThreadFactory exportToParquet = new PhaseThreadFactory(mapParam, TraitementPhase.EXPORT);
		exportToParquet.execute();
		
		if (exportToParquet.getReport().getException() != null)
		{
			message("Erreur export parquet");
			throw exportToParquet.getReport().getException();
		}
		
		message("Fin export parquet");
	}

	/**
	 * Remets les archive déjà en cours de traitement à la phase précédente Créer la
	 * table de pilotage batch si elle n'existe pas déjà
	 * 
	 * @throws ArcException
	 */
	private void maintenanceTablePilotageBatch() throws ArcException {

		// postgres catalog maintenance
		DatabaseMaintenance.maintenancePgCatalogAllNods(dao.getBatchConnection(), FormatSQL.VACUUM_OPTION_FULL);

		// arc pilotage table maintenance
		DatabaseMaintenance.maintenancePilotage(dao.getBatchConnection(), envExecution, FormatSQL.VACUUM_OPTION_NONE);

	}

	/**
	 * Effacer les répertoires de chargement OK KO et ENCOURS
	 * 
	 * @param directory
	 * @param envExecution
	 * @throws IOException
	 */
	private void effacerRepertoireChargement(String directory, String envExecution) throws ArcException {

		message("Déplacements de fichiers");

		// Effacer les fichiers des répertoires OK et KO
		cleanDirectory(directory, envExecution, TraitementEtat.OK);

		cleanDirectory(directory, envExecution, TraitementEtat.KO);

		cleanDirectory(directory, envExecution, TraitementEtat.ENCOURS);

		message("Fin effacement des répertoires");

	}

	/**
	 * delete all files and directory inside the reception_ko, reception_encours and
	 * reception_ko
	 */
	private static void cleanDirectory(String directory, String envExecution, TraitementEtat etat) throws ArcException {
		File f = Paths.get(DirectoryPath.directoryReceptionEtat(directory, envExecution, etat)).toFile();
		if (!f.exists()) {
			return;
		}
		File[] fs = f.listFiles();
		for (File z : fs) {
			if (z.isDirectory()) {
				FileUtilsArc.deleteDirectory(z);
			} else {
				deleteIfArchived(directory, envExecution, z);
			}
		}
	}

	/**
	 * If the file has already been moved in the archive directory by ARC it is safe
	 * to delete it else save it to the archive directory
	 * 
	 * @param repertoire
	 * @param envExecution
	 * @param z
	 * @return
	 * @throws IOException
	 */
	private static void deleteIfArchived(String repertoire, String envExecution, File z) throws ArcException {

		String entrepot = ManipString.substringBeforeFirst(z.getName(), "_");
		String filename = ManipString.substringAfterFirst(z.getName(), "_");

		// ajout d'un garde fou : si le fichier n'est pas archivé : pas touche
		File fCheck = Paths
				.get(DirectoryPath.directoryReceptionEntrepotArchive(repertoire, envExecution, entrepot), filename)
				.toFile();

		if (fCheck.exists()) {
			FileUtilsArc.delete(z);
		} else {
			FileUtilsArc.renameTo(z, fCheck);
		}
	}

	/**
	 * si c'est une reprise de batch déjà en cours, on remet les fichiers en_cours à
	 * l'état précédent dans la table de pilotage
	 * 
	 * @param envExecution
	 * @param repriseEnCOurs
	 * @param recevoir
	 * @throws ArcException
	 */
	private void phaseReceptionExecute() {
		if (dejaEnCours) {
			message("Reprise du traitement des fichiers du batch précédent. Pas de phase reception");
			return;
		}

		message("Reception de nouveaux fichiers");
		PhaseThreadFactory recevoir = new PhaseThreadFactory(mapParam, TraitementPhase.RECEPTION);
		recevoir.execute();
		message("Reception : " + recevoir.getReport().getNbObject() + " objets enregistrés en "
				+ recevoir.getReport().getDuree() + " ms");

	}

	private void phaseInitializationExecute() throws ArcException {

		// la nouvelle initialisation se lance si la date actuelle est postérieure à la
		// date programmée d'initialisation (last_init)
		// on ne la lance que s'il n'y a rien en cours (pas essentiel mais plus
		// sécurisé)
		if ((!dejaEnCours && dao.isInitializationMustTrigger(this.envExecution))) {
			message("Initialisation en cours");

			PhaseThreadFactory initialiser = new PhaseThreadFactory(mapParam, TraitementPhase.INITIALISATION);

			initialiser.execute();

			message("Initialisation terminée : " + initialiser.getReport().getDuree() + " ms");

			dao.execUpdateLastInitialisationTimestamp(envExecution, intervalForInitializationInDay,
					hourToTriggerInitializationInProduction);

			return;
		}

		// if no initialization phase had been run, metadata on executor nods must be
		// synchronized
		executeIfExecutors(this::synchronizeExecutorsMetadata);

	}

	/**
	 * Synchronize executors metadata with coordinator
	 * 
	 * @throws ArcException
	 */
	private void synchronizeExecutorsMetadata() throws ArcException {

		message("Synchronization vers les executeurs en cours");

		new SynchronizeRulesAndMetadataOperation(new Sandbox(dao.getBatchConnection(), this.envExecution))
				.synchroniserSchemaExecutionAllNods();

		message("Synchronization terminé");
	}

	/**
	 * mark if the batch has been interrupted get the list of archives which process
	 * was interrupted to move them back in the input directory
	 * 
	 * @throws ArcException
	 * @throws IOException
	 */
	private void deplacerFichiersNonTraites() throws ArcException {

		List<String> aBouger = exportOn ? //
				dao.execQuerySelectArchiveNotExported(envExecution) //
				: dao.execQuerySelectArchiveEnCours(envExecution);

		dejaEnCours = (!aBouger.isEmpty());

		// si oui, on essaie de recopier les archives dans chargement OK
		if (dejaEnCours) {
			copyPendingFilesOfLastBatchFromArchiveDirectoryToOKDirectory(envExecution, repertoire, aBouger);
		}
		
		// si le s3 est actif, on sauvegarde les archives pending ou KO vers le s3
		List<String> aBougerToS3 = ArcS3.INPUT_BUCKET.isS3Off() ? new ArrayList<>():dao.execQuerySelectArchivePendingOrKO(envExecution);
		if (!aBougerToS3.isEmpty()) {
			savePendingOrKOArchivesToS3(envExecution, repertoire, aBougerToS3);
		}
		
		message("Fin des déplacements de fichiers");

	}
	
	/**
	 * Copy the files from the archive directory to ok directory
	 * 
	 * @param envExecution
	 * @param repertoire
	 * @param aBouger
	 * @throws IOException
	 */
	private void copyPendingFilesOfLastBatchFromArchiveDirectoryToOKDirectory(String envExecution, String repertoire, List<String> aBouger)
			throws ArcException {

		for (String container : aBouger) {
			String entrepotContainer = ManipString.substringBeforeFirst(container, "_");
			String originalContainer = ManipString.substringAfterFirst(container, "_");

			File fIn = Paths
					.get(DirectoryPath.directoryReceptionEntrepotArchive(repertoire, envExecution, entrepotContainer),
							originalContainer)
					.toFile();

			File fOut = Paths.get(DirectoryPath.directoryReceptionEtatOK(repertoire, envExecution), container).toFile();

			try {
				Files.copy(fIn.toPath(), fOut.toPath());
			} catch (IOException e) {
				throw new ArcException(ArcExceptionMessage.FILE_COPY_FAILED, fIn.getAbsolutePath(),
						fOut.getAbsolutePath());
			}
		}
	}

	/**
	 * Copy files to s3 KO directory if archive was KO or pending from previous batch
	 * @param envExecution2
	 * @param repertoire2
	 * @param aBougerToS3
	 * @throws ArcException
	 */
	private void savePendingOrKOArchivesToS3(String envExecution2, String repertoire2, List<String> aBougerToS3) throws ArcException {
		for (String container : aBougerToS3) {
			String entrepotContainer = ManipString.substringBeforeFirst(container, "_");
			String originalContainer = ManipString.substringAfterFirst(container, "_");

			File fIn = Paths
					.get(DirectoryPath.directoryReceptionEntrepotArchive(repertoire, envExecution, entrepotContainer),
							originalContainer)
					.toFile();
			
			if (!fIn.exists())
				continue;
			
			// save files to s3 if not already exist
			String s3ArchiveDirectory = DirectoryPath.s3ReceptionEntrepotKO(envExecution, entrepotContainer);
			ArcS3.INPUT_BUCKET.createDirectory(s3ArchiveDirectory);
			String targetS3File= s3ArchiveDirectory + File.separator + originalContainer;
			if (!ArcS3.INPUT_BUCKET.isExists(targetS3File))
			{
				ArcS3.INPUT_BUCKET.upload(fIn, targetS3File);
			}
			ArcS3.INPUT_BUCKET.closeMinioClient();
			
		}
		
	}

	/**
	 * initalize the arraylist of phases to be looped over and the thread pool per
	 * phase calculated sleep delay between phase
	 * 
	 * @param phases
	 * @param pool
	 * @return
	 */
	private void initializeBatchLoop() {
		int stepNumber = (TraitementPhase.MAPPING.getOrdre() - TraitementPhase.CHARGEMENT.getOrdre()) + 2;
		this.delay = poolingDelay / stepNumber;

		message("Initialisation boucle Chargement->Mapping");

		// initialiser le tableau de phase
		phases.addAll(TraitementPhase.getListPhaseBatchToLoopOver());

		// initialiser le pool de thread par phase
		for (TraitementPhase phase : phases) {
			pool.put(phase, new ArrayList<>());
		}

	}

	/**
	 * start paralell thread
	 * 
	 * @throws ArcException
	 */
	private void executeLoopOverPhases() throws ArcException {

		initializeBatchLoop();

		// boucle de chargement
		message("Début de la boucle d'itération");

		do {

			this.iteration++;

			updateThreadPoolStatus();

			startPhaseThread();

			startMaintenanceThread();

			updateProductionOn();

			updateExit();

			waitAndClear();

		} while (!exit);

		message("Fin de la boucle d'itération");

	}

	private void updateProductionOn() throws ArcException {
		if (iteration % numberOfIterationBewteenCheckTodo == 0) {
			// check and update production on
			isProductionOn();
		}
	}

	// updtate the thread pool by phase by deleting the dead and finished thread
	private void updateThreadPoolStatus() {
		// delete dead thread i.e. keep only living thread in the pool
		Map<TraitementPhase, List<PhaseThreadFactory>> poolToKeep = new HashMap<>();
		for (TraitementPhase phase : phases) {
			poolToKeep.put(phase, new ArrayList<>());
			if (!pool.get(phase).isEmpty()) {
				for (PhaseThreadFactory thread : pool.get(phase)) {
					if (thread.isAlive()) {
						poolToKeep.get(phase).add(thread);
					}
				}
			}
		}
		this.pool = poolToKeep;
	}

	// build and start a new phase thread if the former created thread has died
	private void startPhaseThread() {
		// add new thread and start
		for (TraitementPhase phase : phases) {
			// if no thread in phase, start one
			if (pool.get(phase).isEmpty()) {
				PhaseThreadFactory thread = new PhaseThreadFactory(mapParam, phase);
				thread.start();
				pool.get(phase).add(thread);
			}
			// delay between phases not to overload
			Sleep.sleep(delay);
		}
	}

	private void startMaintenanceThread() {
		if ((iteration % numberOfIterationBewteenDatabaseMaintenanceRoutine) != 0) {
			return;
		}

		message(iteration + ": boucle Chargement->Mapping en cours");
		
		if (maintenance.isAlive()) {
			return;
		}

		message(iteration + ": database maintenance started");
		
		maintenance = new Thread() {
			@Override
			public void run() {
				try {
					DatabaseMaintenance.maintenancePgCatalogAllNods(null, FormatSQL.VACUUM_OPTION_NONE);
					DatabaseMaintenance.maintenancePilotage(null, envExecution, FormatSQL.VACUUM_OPTION_NONE);
				} catch (ArcException e) {
					e.logMessageException();
				}
			}
		};
		maintenance.start();
	}

	/**
	 * exit loop condition
	 * 
	 * @param envExecution
	 * @return
	 */
	private boolean isNothingLeftToDo(String envExecution) {
		boolean isNothingLeftToDo = false;
		if (dao.execQueryAnythingLeftTodo(envExecution) == 0) {
			isNothingLeftToDo = true;
		}
		return isNothingLeftToDo;
	}

	/**
	 * test si la chaine batch est arrétée
	 * 
	 * @return
	 * @throws ArcException
	 */
	private boolean isProductionOn() throws ArcException {
		this.productionOn = dao.execQueryIsProductionOn(this.envExecution);
		return productionOn;
	}

	private void updateExit() {
		if (iteration % numberOfIterationBewteenCheckTodo == 0) {
			// check if batch must exit loop
			// exit if nothing left to do or if the production had been turned OFF
			exit = isNothingLeftToDo(envExecution) || !productionOn;
		}
	}

	private void waitAndClear() {
		Sleep.sleep(delay);
		System.gc();
	}

}
