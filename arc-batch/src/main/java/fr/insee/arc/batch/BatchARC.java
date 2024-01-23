package fr.insee.arc.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
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
import fr.insee.arc.batch.threadrunners.PhaseParameterKeys;
import fr.insee.arc.batch.threadrunners.PhaseThreadFactory;
import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.ArcDateFormat;
import fr.insee.arc.core.service.global.dao.DatabaseMaintenance;
import fr.insee.arc.core.service.global.util.Patch;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.batch.IReturnCode;
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
	Thread maintenance=new Thread();
	private boolean productionOn;
	private boolean exit=false;
	private int iteration = 0;

	private static void message(String msg) {
		LoggerHelper.warn(LOGGER, msg);
	}

	private void initParameters() {

		BDParameters bdParameters=new BDParameters(ArcDatabase.COORDINATOR);
		
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

	}

	/**
	 * Lanceur MAIN arc
	 * 
	 * @param args
	 */
	void execute() {

		// fill the parameters
		initParameters();

		message("Main");

		message("Batch ARC " + properties.fullVersionInformation().toString());

		try {
			this.productionOn = isProductionOn();

			if (!this.productionOn) {

				message("La production est arretée !");

			} else {

				message("Traitement Début");

				// database maintenance on pilotage table so that index won't bloat
				maintenanceTablePilotageBatch();

				// delete work directories and move back files that were pending but not finished
				message("Déplacements de fichiers");

				// on vide les repertoires de chargement OK, KO, ENCOURS
				effacerRepertoireChargement(repertoire, envExecution);

				// des archives n'ont elles pas été traitées jusqu'au bout ?
				deplacerFichiersNonTraites();

				message("Fin des déplacements de fichiers");
				
				// initialize. Phase d'initialisation
				initialize();

				// register file. Phase de reception.
				executePhaseReception(envExecution);

				// execute the main batch process. return false if user had interrupted the process
				executeLoopOverPhases();

				// Delete entry files if no interruption or no problems
				effacerRepertoireChargement(repertoire, envExecution);

				message("Traitement Fin");
				System.exit(STATUS_SUCCESS);
			}

		} catch (Exception ex) {
			LoggerHelper.errorGenTextAsComment(BatchARC.class, "main()", LOGGER, ex);
			System.exit(STATUS_FAILURE_TECHNICAL_WARNING);
		}

		message("Fin du batch");

	}

	/**
	 * Remets les archive déjà en cours de traitement à la phase précédente
	 * Créer la table de pilotage batch si elle n'existe pas déjà
	 * 
	 * @throws ArcException
	 */
	private void maintenanceTablePilotageBatch() throws ArcException {

		// reset the pending files status in pilotage
		BatchArcDao.execQueryResetPendingFilesInPilotageTable(envExecution);

		// create the pilotage batch table if it doesn't exists
		BatchArcDao.execQueryCreatePilotageBatch();

		// postgres catalog maintenance
		DatabaseMaintenance.maintenancePgCatalogAllNods(null, FormatSQL.VACUUM_OPTION_FULL);
		
		// arc pilotage table maintenance
		DatabaseMaintenance.maintenancePilotage(null, envExecution, FormatSQL.VACUUM_OPTION_NONE);
	
	}

	/**
	 * exit loop condition
	 * 
	 * @param envExecution
	 * @return
	 */
	private boolean isNothingLeftToDo(String envExecution) {
		boolean isNothingLeftToDo = false;
		if ( BatchArcDao.execQueryAnythingLeftTodo(envExecution) == 0) {
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
	private static boolean isProductionOn() throws ArcException {
		
		return BatchArcDao.execQueryIsProductionOn();

	}

	/**
	 * Effacer les répertoires de chargement OK KO et ENCOURS
	 * 
	 * @param directory
	 * @param envExecution
	 * @throws IOException
	 */
	private void effacerRepertoireChargement(String directory, String envExecution) throws ArcException {
		if (!this.productionOn) {
			return;
		}
		
		// Effacer les fichiers des répertoires OK et KO
		cleanDirectory(directory, envExecution, TraitementEtat.OK);

		cleanDirectory(directory, envExecution, TraitementEtat.KO);

		cleanDirectory(directory, envExecution, TraitementEtat.ENCOURS);

	}

	/**
	 * delete all files and directory inside the reception_ko, reception_encours and reception_ko
	 */
	private static void cleanDirectory(String directory, String envExecution, TraitementEtat etat)
			throws ArcException {
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
				.get(DirectoryPath.directoryReceptionEntrepotArchive(repertoire, envExecution, entrepot),
						filename)
				.toFile();

		if (fCheck.exists()) {
			FileUtilsArc.delete(z);
		} else {
			FileUtilsArc.renameTo(z, fCheck);
		}
	}

	/**
	 * si c'est une reprise de batch déjà en cours, on remet les fichiers en_cours à
	 * l'état précédent dans la table de piltoage
	 * 
	 * @param envExecution
	 * @param repriseEnCOurs
	 * @param recevoir
	 * @throws ArcException
	 */
	private void executePhaseReception(String envExecution) throws ArcException {
		if (dejaEnCours) {
			message("Reprise des fichiers en cours de traitement");
			BatchArcDao.execQueryResetPendingFilesInPilotageTable(envExecution);
		} else {
			message("Reception de nouveaux fichiers");
			PhaseThreadFactory recevoir = new PhaseThreadFactory(mapParam, TraitementPhase.RECEPTION);
			recevoir.execute();
			message("Reception : " + recevoir.getReport().getNbObject() + " objets enregistrés en "
					+ recevoir.getReport().getDuree() + " ms");
		}

	}

	private void initialize() throws ArcException {
		message("Traitement Début");

		PhaseThreadFactory initialiser = new PhaseThreadFactory(mapParam, TraitementPhase.INITIALISATION);

		String lastInitialize = BatchArcDao.execQueryLastInitialisationTimestamp();

		Date dNow = new Date();
		Date dLastInitialize;

		try {
			dLastInitialize = new SimpleDateFormat(ArcDateFormat.DATE_HOUR_FORMAT_CONVERSION.getApplicationFormat()).parse(lastInitialize);
		} catch (ParseException dateParseException) {
			throw new ArcException(dateParseException, ArcExceptionMessage.BATCH_INITIALIZATION_DATE_PARSE_FAILED);
		}

		// la nouvelle initialisation se lance si la date actuelle est postérieure à la
		// date programmée d'initialisation (last_init)
		// on ne la lance que s'il n'y a rien en cours (pas essentiel mais plus
		// sécurisé)
		if ((!dejaEnCours && dLastInitialize.compareTo(dNow) < 0)) {

			message("Initialisation en cours");

			initialiser.execute();

			message("Initialisation terminée : " + initialiser.getReport().getDuree() + " ms");

			BatchArcDao.execUpdateLastInitialisationTimestamp(intervalForInitializationInDay, hourToTriggerInitializationInProduction);

		}
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
			throws IOException {

		for (String container : aBouger) {
			String entrepotContainer = ManipString.substringBeforeFirst(container, "_");
			String originalContainer = ManipString.substringAfterFirst(container, "_");

			File fIn = Paths.get(
					DirectoryPath.directoryReceptionEntrepotArchive(repertoire, envExecution, entrepotContainer),
					originalContainer).toFile();

			File fOut = Paths.get(DirectoryPath.directoryReceptionEtatOK(repertoire, envExecution), container)
					.toFile();

			Files.copy(fIn.toPath(), fOut.toPath());
		}
	}

	
	/**
	 * mark if the batch has been interrupted
	 * get the list of archives which process was interrupted to move them back in the input directory
	 * @throws ArcException
	 * @throws IOException
	 */
	private void deplacerFichiersNonTraites() throws ArcException, IOException {
		
		List<String> aBouger = BatchArcDao.execQuerySelectArchiveEnCours(envExecution);
		
		dejaEnCours = (!aBouger.isEmpty());

		// si oui, on essaie de recopier les archives dans chargement OK
		if (dejaEnCours) {
			copyFileFromArchiveDirectoryToOK(envExecution, repertoire, aBouger);
		}
	}

	/**
	 * initalize the arraylist of phases to be looped over and the thread pool per
	 * phase
	 * calculated sleep delay between phase
	 * 
	 * @param phases
	 * @param pool
	 * @return
	 */
	private void initializeBatchLoop(List<TraitementPhase> phases,
			Map<TraitementPhase, List<PhaseThreadFactory>> pool) {
		int stepNumber = (TraitementPhase.MAPPING.getOrdre() - TraitementPhase.CHARGEMENT.getOrdre()) + 2;
		this.delay = poolingDelay / stepNumber;

		message("Initialisation boucle Chargement->Mapping");

		// initialiser le tableau de phase
		int startingPhase = TraitementPhase.CHARGEMENT.getOrdre();

		for (TraitementPhase phase : TraitementPhase.values()) {
			if (phase.getOrdre() >= startingPhase) {
				phases.add(phase.getOrdre() - startingPhase, phase);
			}
		}

		// initialiser le pool de thread
		for (TraitementPhase phase : phases) {
			pool.put(phase, new ArrayList<>());
		}

	}

	/**
	 * start paralell thread 
	 * @throws ArcException
	 */
	private void executeLoopOverPhases() throws ArcException
	{
		this.productionOn=isProductionOn();
		
		// test if production is running or exit
		if (!productionOn)
		{
			return;
		}
		
		initializeBatchLoop(phases, pool);

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
		
	}

	
	private void waitAndClear() {
		Sleep.sleep(delay);
		System.gc();
	}

	private void updateProductionOn() throws ArcException {
		if (iteration % numberOfIterationBewteenCheckTodo == 0) {
			// check if production on
			this.productionOn = isProductionOn();
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
		this.pool=poolToKeep;
	}

	// build and start a new phase thread if the former created thread has died
	private void startPhaseThread()
	{
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
		if (iteration % numberOfIterationBewteenDatabaseMaintenanceRoutine == 0) {
			if (!maintenance.isAlive()) {
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
		}
	}
	
	private void updateExit() {
		if (iteration % numberOfIterationBewteenCheckTodo == 0) {
			// check if batch must exit loop
			// exit if nothing left to do or if the production had been turned OFF
			exit = isNothingLeftToDo(envExecution) || !productionOn;
		}
	}
	
}
