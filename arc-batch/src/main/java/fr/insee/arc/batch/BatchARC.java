package fr.insee.arc.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.arc.batch.dao.BatchArcDao;
import fr.insee.arc.batch.operation.PhaseInitializationOperation;
import fr.insee.arc.batch.threadrunners.PhaseParameterKeys;
import fr.insee.arc.batch.threadrunners.PhaseThreadFactory;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.DatabaseMaintenance;
import fr.insee.arc.core.service.global.util.Patch;
import fr.insee.arc.core.service.kubernetes.ApiManageExecutorDatabase;
import fr.insee.arc.core.service.p0initialisation.metadata.SynchronizeRulesAndMetadataOperation;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.batch.IReturnCode;
import fr.insee.arc.utils.consumer.ThrowingRunnable;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
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

	/**
	 * Lanceur MAIN arc
	 * 
	 * @param args
	 */
	void execute() {

		try {

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

		message("Fin du batch");

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

	private void batchParametersGet() {

		message("Récupération des paramètres du batch");

		BDParameters bdParameters = new BDParameters(ArcDatabase.COORDINATOR);

		boolean keepInDatabase = Boolean
				.parseBoolean(bdParameters.getString(null, "LanceurARC.keepInDatabase", "false"));

		// pour le batch en cours, l'ensemble des enveloppes traitées ne peut pas
		// excéder une certaine taille
		int tailleMaxReceptionEnMb = bdParameters.getInt(null, "LanceurARC.tailleMaxReceptionEnMb", 10);

		// Maximum number of files to load
		int maxFilesToLoad = bdParameters.getInt(null, "LanceurARC.maxFilesToLoad", 101);

		// Maximum number of files processed in each phase iteration
		int maxFilesPerPhase = bdParameters.getInt(null, "LanceurARC.maxFilesPerPhase", 1000000);

		// fréquence à laquelle les phases sont démarrées
		this.poolingDelay = bdParameters.getInt(null, "LanceurARC.poolingDelay", 1000);

		// heure d'initalisation en production
		hourToTriggerInitializationInProduction = bdParameters.getInt(null,
				"ApiService.HEURE_INITIALISATION_PRODUCTION", 22);

		// interval entre chaque initialisation en nb de jours
		intervalForInitializationInDay = bdParameters.getInt(null, "LanceurARC.INTERVAL_JOUR_INITIALISATION", 7);

		// nombre d'iteration de la boucle batch entre chaque routine de maintenance de
		// la base de données
		numberOfIterationBewteenDatabaseMaintenanceRoutine = bdParameters.getInt(null,
				"LanceurARC.DATABASE_MAINTENANCE_ROUTINE_INTERVAL", 500);

		// nombre d'iteration de la boucle batch entre chaque routine de vérification du
		// reste à faire
		numberOfIterationBewteenCheckTodo = bdParameters.getInt(null, "LanceurARC.DATABASE_CHECKTODO_ROUTINE_INTERVAL",
				10);

		// either we take env and envExecution from database or properties
		// default is from properties
		if (Boolean.parseBoolean(bdParameters.getString(null, "LanceurARC.envFromDatabase", "false"))) {
			envExecution = bdParameters.getString(null, "LanceurARC.envExecution", "arc_prod");
		} else {
			envExecution = properties.getBatchExecutionEnvironment();
		}

		envExecution = Patch.normalizeSchemaName(envExecution);

		repertoire = properties.getBatchParametersDirectory();

		mapParam.put(PhaseParameterKeys.KEY_FOR_DIRECTORY_LOCATION, repertoire);
		mapParam.put(PhaseParameterKeys.KEY_FOR_BATCH_CHUNK_ID, new SimpleDateFormat("yyyyMMddHH").format(new Date()));
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

		message("Volaile database : "+volatileOn);
		
		message("Main");
		message("Batch ARC " + properties.fullVersionInformation().toString());

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

		BatchArcDao.execQueryResetPendingFilesInPilotageTable(envExecution);

		// if volatile mode on, put back all the not fully proceeded files in reception
		// phase
		executeIfVolatile(() -> BatchArcDao.execQueryResetPendingFilesInPilotageTableVolatile(envExecution));
	}

	private void executorsDatabaseCreate() throws ArcException {
		message(ApiManageExecutorDatabase.delete().toString());
		message(ApiManageExecutorDatabase.create().toString());
		
		Sleep.sleep(20000);
	}

	/**
	 * Delete the volatile executor database Can only happen if executors are
	 * declared on kubernetes
	 * 
	 * @throws ArcException
	 */
	private void executorsDatabaseDelete() throws ArcException {
		message(ApiManageExecutorDatabase.delete().toString());
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

		executeIfVolatile(this::executorsDatabaseDelete);

		message("Traitement Fin");
		System.exit(STATUS_SUCCESS);

	}

	/**
	 * Export business mapping tables to parquet Only in volatile mode
	 */
	private void exportToParquet() {
		PhaseThreadFactory exportToParquet = new PhaseThreadFactory(mapParam, TraitementPhase.EXPORT);
		exportToParquet.execute();
		message("Fin export parquet");
	}

	/**
	 * Remets les archive déjà en cours de traitement à la phase précédente Créer la
	 * table de pilotage batch si elle n'existe pas déjà
	 * 
	 * @throws ArcException
	 */
	private void maintenanceTablePilotageBatch() throws ArcException {

		// create the pilotage batch table if it doesn't exists
		BatchArcDao.execQueryCreatePilotageBatch();

		// postgres catalog maintenance
		DatabaseMaintenance.maintenancePgCatalogAllNods(null, FormatSQL.VACUUM_OPTION_FULL);

		// arc pilotage table maintenance
		DatabaseMaintenance.maintenancePilotage(null, envExecution, FormatSQL.VACUUM_OPTION_NONE);

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
		if ((!dejaEnCours && PhaseInitializationOperation.isInitializationMustTrigger())) {
			message("Initialisation en cours");

			PhaseThreadFactory initialiser = new PhaseThreadFactory(mapParam, TraitementPhase.INITIALISATION);

			initialiser.execute();

			message("Initialisation terminée : " + initialiser.getReport().getDuree() + " ms");

			BatchArcDao.execUpdateLastInitialisationTimestamp(intervalForInitializationInDay,
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

		new SynchronizeRulesAndMetadataOperation(new Sandbox(null, this.envExecution))
				.synchroniserSchemaExecutionAllNods();

		message("Synchronization terminé");
	}

	/**
	 * Copy the files from the archive directory to ok directory
	 * 
	 * @param envExecution
	 * @param repertoire
	 * @param aBouger
	 * @throws IOException
	 */
	private void copyFileFromArchiveDirectoryToOK(String envExecution, String repertoire, List<String> aBouger)
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
	 * mark if the batch has been interrupted get the list of archives which process
	 * was interrupted to move them back in the input directory
	 * 
	 * @throws ArcException
	 * @throws IOException
	 */
	private void deplacerFichiersNonTraites() throws ArcException {

		List<String> aBouger = exportOn ? //
				BatchArcDao.execQuerySelectArchiveNotExported(envExecution) //
				: BatchArcDao.execQuerySelectArchiveEnCours(envExecution);

		dejaEnCours = (!aBouger.isEmpty());

		// si oui, on essaie de recopier les archives dans chargement OK
		if (dejaEnCours) {
			copyFileFromArchiveDirectoryToOK(envExecution, repertoire, aBouger);
		}

		message("Fin des déplacements de fichiers");

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
		if (BatchArcDao.execQueryAnythingLeftTodo(envExecution) == 0) {
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
		this.productionOn = BatchArcDao.execQueryIsProductionOn();
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
