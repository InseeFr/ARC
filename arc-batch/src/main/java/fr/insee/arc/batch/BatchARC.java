package fr.insee.arc.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.arc.batch.threadrunners.PhaseParameterKeys;
import fr.insee.arc.batch.threadrunners.PhaseThreadFactory;
import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.api.ApiReceptionService;
import fr.insee.arc.core.service.api.ApiService;
import fr.insee.arc.core.service.api.query.ServiceDatabaseMaintenance;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.batch.IReturnCode;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
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
	private static HashMap<String, String> mapParam = new HashMap<>();

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
	private int hourToTriggerInitializationInProduction;

	// interval entre chaque initialisation en nb de jours
	private Integer intervalForInitializationInDay;

	// nombre de runner maximum pour une phase donnée (cas de blocage)
	private Integer maxNumberOfThreadsOfTheSamePhaseAtTheSameTime;

	// nombre d'itération de la boucle batch au bout duquel le batch vérifie s'il y
	// a un blocage
	// et si un nouveau runner doit etre lancé
	private Integer numberOfIterationBewteenBlockageCheck;

	// nombre d'iteration de la boucle batch entre chaque routine de maintenance de
	// la base de données
	private Integer numberOfIterationBewteenDatabaseMaintenanceRoutine;

	// nombre d'iteration de la boucle batch entre chaque routine de vérification du
	// reste à faire
	private Integer numberOfIterationBewteenCheckTodo;

	// nombre de pods utilisés par ARC
	private Integer numberOfPods;

	// true = the batch will resume the process from a formerly interrupted batch
	// false = the batch will proceed to a new load
	// Maintenance initialization process can only occur in this case
	private boolean dejaEnCours;

	private static void message(String msg) {
		StaticLoggerDispatcher.warn(msg, LOGGER);
	}

	private void message(String msg, int iteration) {
		if (iteration % numberOfIterationBewteenBlockageCheck == 0) {
			message(msg);
		}
	}

	private void initParameters() {

		boolean keepInDatabase = Boolean
				.parseBoolean(BDParameters.getString(null, "LanceurARC.keepInDatabase", "false"));

		// pour le batch en cours, l'ensemble des enveloppes traitées ne peut pas
		// excéder une certaine taille
		int tailleMaxReceptionEnMb = BDParameters.getInt(null, "LanceurARC.tailleMaxReceptionEnMb", 10);

		// Maximum number of files to load
		int maxFilesToLoad = BDParameters.getInt(null, "LanceurARC.maxFilesToLoad", 101);

		// Maximum number of files processed in each phase iteration
		int maxFilesPerPhase = BDParameters.getInt(null, "LanceurARC.maxFilesPerPhase", 1000000);

		// fréquence à laquelle les phases sont démarrées
		this.poolingDelay = BDParameters.getInt(null, "LanceurARC.poolingDelay", 1000);

		// heure d'initalisation en production
		hourToTriggerInitializationInProduction = BDParameters.getInt(null,
				"ApiService.HEURE_INITIALISATION_PRODUCTION", 22);

		// interval entre chaque initialisation en nb de jours
		intervalForInitializationInDay = BDParameters.getInt(null, "LanceurARC.INTERVAL_JOUR_INITIALISATION", 7);

		// nombre de runner maximum pour une phase donnée (cas de blocage)
		maxNumberOfThreadsOfTheSamePhaseAtTheSameTime = BDParameters.getInt(null,
				"LanceurARC.MAX_PARALLEL_RUNNER_PER_PHASE", 1);

		// nombre d'itération de la boucle batch au bout duquel le batch vérifie s'il y
		// a un blocage
		// et si un nouveau runner doit etre lancé
		numberOfIterationBewteenBlockageCheck = BDParameters.getInt(null, "LanceurARC.PARALLEL_LOCK_CHECK_INTERVAL",
				120);

		// nombre d'iteration de la boucle batch entre chaque routine de maintenance de
		// la base de données
		numberOfIterationBewteenDatabaseMaintenanceRoutine = BDParameters.getInt(null,
				"LanceurARC.DATABASE_MAINTENANCE_ROUTINE_INTERVAL", 500);

		// nombre d'iteration de la boucle batch entre chaque routine de vérification du
		// reste à faire
		numberOfIterationBewteenCheckTodo = BDParameters.getInt(null, "LanceurARC.DATABASE_CHECKTODO_ROUTINE_INTERVAL",
				10);

		// the number of pods declared for scalability
		numberOfPods = UtilitaireDao.get(0).computeNumberOfExecutorNods();

		// the metadata schema
		String env;

		// either we take env and envExecution from database or properties
		// default is from properties
		if (Boolean.parseBoolean(BDParameters.getString(null, "LanceurARC.envFromDatabase", "false"))) {
			env = BDParameters.getString(null, "LanceurARC.env", ApiService.IHM_SCHEMA);
			envExecution = BDParameters.getString(null, "LanceurARC.envExecution", "arc_prod");
		} else {
			env = properties.getBatchArcEnvironment();
			envExecution = properties.getBatchExecutionEnvironment();
		}

		envExecution = envExecution.replace(".", "_");

		repertoire = properties.getBatchParametersDirectory();

		mapParam.put(PhaseParameterKeys.KEY_FOR_DIRECTORY_LOCATION, repertoire);
		mapParam.put(PhaseParameterKeys.KEY_FOR_BATCH_CHUNK_ID, new SimpleDateFormat("yyyyMMddHH").format(new Date()));
		mapParam.put(PhaseParameterKeys.KEY_FOR_METADATA_ENVIRONMENT, env);
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

		Thread maintenance = new Thread();

		try {

			boolean productionOn = productionOn();

			if (!productionOn) {

				message("La production est arretée !");

			} else {

				resetPending(envExecution);

				maintenanceTablePilotageBatch();

				message("Déplacements de fichiers");

				// on vide les repertoires de chargement OK, KO, ENCOURS
				effacerRepertoireChargement(repertoire, envExecution);

				// des archives n'ont elles pas été traitées jusqu'au bout ?
				ArrayList<String> aBouger = new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
						new ArcPreparedStatementBuilder(
								"select distinct container from " + envExecution + ".pilotage_fichier where etape=1")))
						.mapContent().get("container");

				dejaEnCours = (aBouger != null);

				// si oui, on essaie de recopier les archives dans chargement OK
				if (dejaEnCours) {
					copyFileFromArchiveDirectoryToOK(envExecution, repertoire, aBouger);

				}
				message("Fin des déplacements de fichiers");

				message("Traitement Début");

				// initialize. Phase d'initialisation
				initialize();

				// register file. Phase de reception.
				receive(envExecution, dejaEnCours);

				productionOn = productionOn();
				// Vérifier si la production est activée
				if (productionOn) {

					int delay = poolingDelay / 6;
					boolean exit = false;

					message("Début boucle Chargement->Mapping");

					// initialiser le tableau de phase
					int startingPhase = TraitementPhase.CHARGEMENT.getOrdre();

					ArrayList<TraitementPhase> phases = new ArrayList<>();
					for (TraitementPhase phase : TraitementPhase.values()) {
						if (phase.getOrdre() >= startingPhase) {
							phases.add(phase.getOrdre() - startingPhase, phase);
						}
					}

					// initialiser le pool de thread
					HashMap<TraitementPhase, ArrayList<PhaseThreadFactory>> pool = new HashMap<>();
					for (TraitementPhase phase : phases) {
						pool.put(phase, new ArrayList<>());
					}

					// boucle de chargement
					int iteration = 0;
					message("> iteration " + iteration);

					do {

						iteration++;

						message("> batch lock check iteration : " + iteration, iteration);

						// delete dead thread i.e. keep only living thread in the pool
						HashMap<TraitementPhase, ArrayList<PhaseThreadFactory>> poolToKeep = new HashMap<>();
						for (TraitementPhase phase : phases) {
							poolToKeep.put(phase, new ArrayList<>());
							if (!pool.get(phase).isEmpty()) {
								for (PhaseThreadFactory thread : pool.get(phase)) {
									if (thread.isAlive()) {
										poolToKeep.get(phase).add(thread);
										message(phase + " is still alive", iteration);
									}
								}
							}
						}
						pool = poolToKeep;

						// add new thread and start

						HashMap<TraitementPhase, Integer> elligibleFiles = new HashMap<TraitementPhase, Integer>();

						for (TraitementPhase phase : phases) {
							// if no thread in phase, start one
							if (pool.get(phase).isEmpty()) {
								PhaseThreadFactory a = new PhaseThreadFactory(mapParam, phase);
								a.start();
								pool.get(phase).add(a);

								message(">> start " + phase, iteration);

							} else {
								// if a thread is blocked, add the right thread

								// we test blocked thread every nth iteration and only if extra runners are
								// allowed
								if (iteration % numberOfIterationBewteenBlockageCheck == 0
										&& (pool.get(phase).size() < maxNumberOfThreadsOfTheSamePhaseAtTheSameTime)) {

									// check if all the phase threads are blocked
									boolean blocked = true;
									// iterate thru the thread pool
									for (PhaseThreadFactory thread : pool.get(phase)) {
										// if one thread is not considered as blocked, exit, nothing to do
										if (!thread.isBlocked()) {
											blocked = false;
											break;
										}
									}

									// if ALL threads for the phase are blocked, something has to be done
									if (blocked) {

										message(">> blocked " + phase, iteration);

										// retrieve what phase still have some things to do
										// this will retrieved only once
										if (elligibleFiles.isEmpty()) {
											elligibleFiles = elligible();
										}

										// now start the right thread
										// we will iterate through the phase to see what thread to start

										// if something to do in previous phase and that the thread is blocked
										// start a new thread for the phase
										if (elligibleFiles.get(phase.previousPhase()) != null) {
											// can start a new thread if no more than the
											// maxNumberOfThreadsOfTheSamePhaseAtTheSameTime in the stack

											PhaseThreadFactory a = new PhaseThreadFactory(mapParam, phase);
											a.start();
											pool.get(phase).add(a);

											message(">> starting new " + phase, iteration);
										} else {

											boolean nothingToDoInPrevious = true;
											// if nothing to do in the previous phases
											for (int i = phases.indexOf(phase) - 2; i >= 0; i--) {
												if (elligibleFiles.get(phases.get(i)) != null) {
													nothingToDoInPrevious = false;
													break;
												}
											}

											if (nothingToDoInPrevious) {
												message(">> starting new reception", iteration);
												receive(envExecution, false);
												// exit loop if new files are recieved not to trigger it several times
												break;
											}

										}
									}

								}
							}
							// delay between phases not to overload
							Sleep.sleep(delay);
						}

						if (iteration % numberOfIterationBewteenDatabaseMaintenanceRoutine == 0) {
							if (!maintenance.isAlive()) {
								message(iteration + ": database maintenance started");
								maintenance = new Thread() {
									@Override
									public void run() {
										for (int poolIndex = 0; poolIndex <= numberOfPods; poolIndex++) {
											ServiceDatabaseMaintenance.maintenanceDatabaseClassic(poolIndex, null, envExecution);
										}
									}
								};
								maintenance.start();
							}
						}

						if (iteration % numberOfIterationBewteenCheckTodo == 0) {
							// check if production on
							productionOn = productionOn();

							// check if batch must exit loop
							// exit if nothing left to do or if the production had been turned OFF
							exit = isNothingLeftToDo(envExecution) || !productionOn;
						}

						Sleep.sleep(delay);
						System.gc();

					} while (!exit);

					if (productionOn) {
						// Effacer les fichiers du répertoire OK
						effacerRepertoireChargement(repertoire, envExecution);
					}

				}

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
	 * Créer la table de pilotage batch si elle n'existe pas déjà
	 * 
	 * @throws ArcException
	 */
	private void maintenanceTablePilotageBatch() throws ArcException {

		// création de la table si elle n'existe pas
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("\n CREATE TABLE IF NOT EXISTS arc.pilotage_batch (last_init text, operation text); ");
		requete.append(
				"\n insert into arc.pilotage_batch select '1900-01-01:00','O' where not exists (select 1 from arc.pilotage_batch); ");
		UtilitaireDao.get("arc").executeRequest(null, requete);

		for (int poolIndex = 0; poolIndex <= numberOfPods; poolIndex++) {
			// Maintenance full du catalog
			ServiceDatabaseMaintenance.maintenancePgCatalog(poolIndex, null, FormatSQL.VACUUM_OPTION_FULL);
			// maintenance des tables métier de la base de données
			ServiceDatabaseMaintenance.maintenanceDatabaseClassic(poolIndex, null, envExecution);
		}
	}

	/**
	 * exit loop condition
	 * 
	 * @param envExecution
	 * @return
	 */
	private boolean isNothingLeftToDo(String envExecution) {
		boolean isNothingLeftToDo = false;
		if (UtilitaireDao.get("arc").getInt(null, new ArcPreparedStatementBuilder("select count(*) from (select 1 from "
				+ envExecution + ".pilotage_fichier where etape=1 limit 1) ww")) == 0) {
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
	private static boolean productionOn() throws ArcException {
		return UtilitaireDao.get("arc").hasResults(null,
				new ArcPreparedStatementBuilder("select 1 from arc.pilotage_batch where operation='O'"));
	}

	/**
	 * Effacer les répertoires de chargement OK KO et ENCOURS
	 * 
	 * @param directory
	 * @param envExecution
	 * @throws IOException
	 */
	private static void effacerRepertoireChargement(String directory, String envExecution) throws ArcException {

		// Effacer les fichiers des répertoires OK et KO
		String envDirectory = envExecution.replace(".", "_").toUpperCase();

		cleanDirectory(directory, envExecution, envDirectory, TraitementEtat.OK);

		cleanDirectory(directory, envExecution, envDirectory, TraitementEtat.KO);

		cleanDirectory(directory, envExecution, envDirectory, TraitementEtat.ENCOURS);

	}

	private static void cleanDirectory(String directory, String envExecution, String envDirectory,
			TraitementEtat etat) throws ArcException {
		File f = Paths.get(ApiReceptionService.directoryReceptionEtat(directory, envDirectory, etat)).toFile();
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
				.get(ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, envExecution, entrepot),
						filename)
				.toFile();

		if (fCheck.exists()) {
			FileUtilsArc.delete(z);
		} else {
			FileUtilsArc.renameTo(z, fCheck);
		}
	}

	/**
	 * 
	 * @param envExecution
	 * @throws ArcException
	 */
	private static void resetPending(String envExecution) throws ArcException {
		// delete files that are en cours
		StringBuilder query = new StringBuilder();
		query.append("\n DELETE FROM " + envExecution + ".pilotage_fichier ");
		query.append("\n WHERE etape=1 AND etat_traitement='{" + TraitementEtat.ENCOURS + "}' ");
		query.append(";");

		// update these files to etape=1
		query.append("\n UPDATE " + envExecution + ".pilotage_fichier ");
		query.append("\n set etape=1 ");
		query.append("\n WHERE etape=3");
		query.append(";");

		UtilitaireDao.get("arc").executeBlock(null, query);

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
	private static void receive(String envExecution, boolean repriseEnCOurs) throws ArcException {
		if (repriseEnCOurs) {
			message("Reprise des fichiers en cours de traitement");
			resetPending(envExecution);
		} else {
			message("Reception de nouveaux fichiers");
			PhaseThreadFactory recevoir = new PhaseThreadFactory(mapParam, TraitementPhase.RECEPTION);
			recevoir.execute();
			message("Reception : " + recevoir.getReport().getNbObject() + " objets enregistrés en "
					+ recevoir.getReport().getDuree() + " ms");
		}

	}

	private void initialize() throws ArcException {
		PhaseThreadFactory initialiser = new PhaseThreadFactory(mapParam, TraitementPhase.INITIALISATION);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");

		String lastInitialize = null;
		lastInitialize = UtilitaireDao.get("arc").getString(null,
				new ArcPreparedStatementBuilder("select last_init from arc.pilotage_batch "));

		Date dNow = new Date();
		Date dLastInitialize;

		try {
			dLastInitialize = dateFormat.parse(lastInitialize);
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

			UtilitaireDao.get("arc").executeRequest(null,
					new ArcPreparedStatementBuilder(
							"update arc.pilotage_batch set last_init=to_char(current_date+interval '"
									+ intervalForInitializationInDay + " days','yyyy-mm-dd')||':"
									+ hourToTriggerInitializationInProduction
									+ "' , operation=case when operation='R' then 'O' else operation end;"));
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
	private void copyFileFromArchiveDirectoryToOK(String envExecution, String repertoire, ArrayList<String> aBouger)
			throws IOException {

		for (String container : aBouger) {
			String entrepotContainer = ManipString.substringBeforeFirst(container, "_");
			String originalContainer = ManipString.substringAfterFirst(container, "_");

			File fIn = Paths.get(
					ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, envExecution, entrepotContainer),
					originalContainer).toFile();

			File fOut = Paths.get(ApiReceptionService.directoryReceptionEtatOK(repertoire, envExecution), container)
					.toFile();

			Files.copy(fIn.toPath(), fOut.toPath());
		}
	}

	/**
	 * return if there is
	 * 
	 * @return
	 * @throws ArcException
	 * @throws ArcException
	 */
	private HashMap<TraitementPhase, Integer> elligible() throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append("SELECT phase_traitement, count(*) as n ");
		query.append("FROM " + this.envExecution + ".pilotage_fichier ");
		query.append("WHERE etape=1 and etat_traitement!='{" + TraitementEtat.ENCOURS + "}' ");
		query.append("GROUP BY phase_traitement ");
		query.append("HAVING COUNT(*)>0 ");

		HashMap<String, String> keyValue = new GenericBean(UtilitaireDao.get("arc").executeRequest(null, query))
				.keyValue();
		HashMap<TraitementPhase, Integer> r = new HashMap<>();

		for (String k : keyValue.keySet()) {
			r.put(TraitementPhase.valueOf(k), Integer.parseInt(keyValue.get(k)));
		}
		return r;
	}

}
