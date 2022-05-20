package fr.insee.arc.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.arc.batch.threadRunners.ArcThreadFactory;
import fr.insee.arc.batch.threadRunners.parameter.ParameterKey;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.ApiReceptionService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
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
public class BatchARC {
	private static final Logger LOGGER = LogManager.getLogger(BatchARC.class);
	static HashMap<String, String> mapParam = new HashMap<>();

	/**
	 * variable dateInitialisation si vide (ou si date du jour+1 depassé à 20h), je
	 * lance initialisation et j'initialise dateInitialisation à la nouvelle date du
	 * jour puis une fois terminé, je lancent la boucle des batchs si date du jour+1
	 * depassé a 20h, - j'indique aux autre batchs de s'arreter - une fois arretés,
	 * je met tempo à la date du jour - je lance initialisation etc.
	 */

	@Autowired
	PropertiesHandler properties;

	// the metadata schema
	String env;

	// the sandbox schema where batch process runs
	String envExecution;

	// file directory
	String repertoire;

	// keepInDatabase = est-ce qu'on garde les données des phases intérmédiaires en
	// base ?
	// false en production
	private static boolean keepInDatabase;

	// pour le batch en cours, l'ensemble des enveloppes traitées ne peut pas
	// excéder une certaine taille
	protected static int tailleMaxReceptionEnMb;

	// Maximum number of files to load
	protected static int maxFilesToLoad;

	// Maximum number of files processed in each phase iteration
	protected static int maxFilesPerPhase;

	// fréquence à laquelle les phases sont démarrées
	private static int poolingDelay;

	// heure d'initalisation en production
	private static int hourToTriggerInitializationInProduction;

	// interval entre chaque initialisation en nb de jours
	private static Integer intervalForInitializationInDay;

	// nombre de runner maximum pour une phase donnée (cas de blocage)
	private static Integer maxNumberOfThreadsOfTheSamePhaseAtTheSameTime;
	
	// nombre d'itération de la boucle batch au bout duquel le batch vérifie s'il y a un blocage
	// et si un nouveau runner doit etre lancé
	private static Integer numberOfIterationBewteenBlockageCheck;

	// true = the batch will resume the process from a formerly interrupted batch
	// false = the batch will proceed to a new load
	// Maintenance initialization process can only occur in this case
	private static boolean dejaEnCours;

	public static void message(String msg) {
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "  " + msg);
	}
	
	public static void message(String msg, int iteration) {
		if (iteration%numberOfIterationBewteenBlockageCheck==0)
		{
			message(msg);
		}
	}
	
	private void initParameters() {

		keepInDatabase = Boolean.parseBoolean(BDParameters.getString(null, "LanceurARC.keepInDatabase", "false"));

		// pour le batch en cours, l'ensemble des enveloppes traitées ne peut pas
		// excéder une certaine taille
		tailleMaxReceptionEnMb = BDParameters.getInt(null, "LanceurARC.tailleMaxReceptionEnMb", 10);

		// Maximum number of files to load
		maxFilesToLoad = BDParameters.getInt(null, "LanceurARC.maxFilesToLoad", 101);

		// Maximum number of files processed in each phase iteration
		maxFilesPerPhase = BDParameters.getInt(null, "LanceurARC.maxFilesPerPhase", 1000000);

		// fréquence à laquelle les phases sont démarrées
		poolingDelay = BDParameters.getInt(null, "LanceurARC.poolingDelay", 1000);

		// heure d'initalisation en production
		hourToTriggerInitializationInProduction = BDParameters.getInt(null,
				"ApiService.HEURE_INITIALISATION_PRODUCTION", 22);

		// interval entre chaque initialisation en nb de jours
		intervalForInitializationInDay = BDParameters.getInt(null, "LanceurARC.INTERVAL_JOUR_INITIALISATION", 7);

		// nombre de runner maximum pour une phase donnée (cas de blocage)
		maxNumberOfThreadsOfTheSamePhaseAtTheSameTime = BDParameters.getInt(null,
				"LanceurARC.MAX_PARALLEL_RUNNER_PER_PHASE", 1);
		
		// nombre d'itération de la boucle batch au bout duquel le batch vérifie s'il y a un blocage
		// et si un nouveau runner doit etre lancé
		numberOfIterationBewteenBlockageCheck = BDParameters.getInt(null,
				"LanceurARC.PARALLEL_LOCK_CHECK_INTERVAL", 120);

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

		mapParam.put(ParameterKey.KEY_FOR_DIRECTORY_LOCATION, repertoire);
		mapParam.put(ParameterKey.KEY_FOR_BATCH_CHUNK_ID, new SimpleDateFormat("yyyyMMddHH").format(new Date()));
		mapParam.put(ParameterKey.KEY_FOR_METADATA_ENVIRONMENT, env);
		mapParam.put(ParameterKey.KEY_FOR_EXECUTION_ENVIRONMENT, envExecution);
		mapParam.put(ParameterKey.KEY_FOR_MAX_SIZE_RECEPTION, String.valueOf(tailleMaxReceptionEnMb));
		mapParam.put(ParameterKey.KEY_FOR_MAX_FILES_TO_LOAD, String.valueOf(maxFilesToLoad));
		mapParam.put(ParameterKey.KEY_FOR_MAX_FILES_PER_PHASE, String.valueOf(maxFilesPerPhase));
		mapParam.put(ParameterKey.KEY_FOR_KEEP_IN_DATABASE, String.valueOf(keepInDatabase));

		message(mapParam.toString());

	}

	/**
	 * Lanceur MAIN arc
	 * 
	 * @param args
	 */
	public void execute(String[] args) {

		// fill the parameters
		initParameters();

		message("Main");

		message("Batch ARC " + properties.fullVersionInformation().toString());

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
						new PreparedStatementBuilder(
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
					HashMap<TraitementPhase, ArrayList<ArcThreadFactory>> pool = new HashMap<>();
					for (TraitementPhase phase : phases) {
						pool.put(phase, new ArrayList<>());
					}

					// boucle de chargement
					int iteration = 0;
					message("> iteration " + iteration);
					
					do {

						iteration++;
						
						message("> batch lock check iteration : "+iteration, iteration);
						
						// delete dead thread i.e. keep only living thread in the pool
						HashMap<TraitementPhase, ArrayList<ArcThreadFactory>> poolToKeep = new HashMap<>();
						for (TraitementPhase phase : phases) {
							poolToKeep.put(phase, new ArrayList<>());
							if (!pool.get(phase).isEmpty()) {
								for (ArcThreadFactory thread : pool.get(phase)) {
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
								ArcThreadFactory a = new ArcThreadFactory(mapParam, phase);
								a.start();
								pool.get(phase).add(a);

								message(">> start " + phase, iteration);

							} else {
								// if a thread is blocked, add the right thread

								// we test blocked thread every nth iteration and only if extra runners are
								// allowed
								if (iteration % numberOfIterationBewteenBlockageCheck == 0
										&& (pool.get(phase).size() < maxNumberOfThreadsOfTheSamePhaseAtTheSameTime)) {
									iteration = 0;

									// check if all the phase threads are blocked
									boolean blocked = true;
									// iterate thru the thread pool
									for (ArcThreadFactory thread : pool.get(phase)) {
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
											
											ArcThreadFactory a = new ArcThreadFactory(mapParam, phase);
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
												ApiService.maintenancePgCatalog(null, "freeze");
												ApiService.maintenancePilotage(null, envExecution, "freeze");
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

						//check if production on 
						productionOn=productionOn();

						// check if batch must exit loop
						// exit if nothing left to do or if the production had been turned OFF
						exit = isNothingLeftToDo(envExecution) || !productionOn;
						
						Sleep.sleep(delay);
						System.gc();
						
					} while (!exit);

					if (productionOn) {
						// Effacer les fichiers du répertoire OK
						effacerRepertoireChargement(repertoire, envExecution);
					}

				}

				// Maintenance du catalog
				ApiService.maintenancePgCatalog(null, "full");
				message("Traitement Fin");

				if (args != null && args.length > 0 && args[0].equals("noExit")) {
					message("No Exit");
				} else {
					System.exit(0);
				}

			}

		} catch (Exception ex) {
			LoggerHelper.errorGenTextAsComment(BatchARC.class, "main()", LOGGER, ex);
			System.exit(202);
		}

		message("Fin du batch");

	}

	/**
	 * Créer la table de pilotage batch si elle n'existe pas déjà
	 * 
	 * @throws SQLException
	 * @throws Exception
	 */
	public void maintenanceTablePilotageBatch() throws SQLException {

		// création de al table si elle n'existe pas
		PreparedStatementBuilder requete = new PreparedStatementBuilder();
		requete.append("\n CREATE TABLE IF NOT EXISTS arc.pilotage_batch (last_init text, operation text); ");
		requete.append(
				"\n insert into arc.pilotage_batch select '1900-01-01:00','O' where not exists (select 1 from arc.pilotage_batch); ");
		UtilitaireDao.get("arc").executeRequest(null, requete);

		// opération de maintenance
		message("Maintenance pilotage");
		ApiService.maintenancePilotage(null, envExecution, "freeze");
		message("Fin de Maintenance pilotage");

	}

	/**
	 * exit loop condition
	 * 
	 * @param envExecution
	 * @return
	 */
	public boolean isNothingLeftToDo(String envExecution) {
		boolean isNothingLeftToDo = false;
		if (UtilitaireDao.get("arc").getInt(null, new PreparedStatementBuilder("select count(*) from (select 1 from "
				+ envExecution + ".pilotage_fichier where etape=1 limit 1) ww")) == 0) {
			isNothingLeftToDo = true;
		}
		return isNothingLeftToDo;
	}

	/**
	 * test si la chaine batch est arrétée
	 * 
	 * @return
	 * @throws Exception
	 */
	public static boolean productionOn() throws Exception {
		return UtilitaireDao.get("arc").hasResults(null,
				new PreparedStatementBuilder("select 1 from arc.pilotage_batch where operation='O'"));
	}

	/**
	 * Effacer les répertoires de chargement OK KO et ENCOURS
	 * 
	 * @param directory
	 * @param envExecution
	 * @throws IOException
	 * @throws Exception
	 */
	public static void effacerRepertoireChargement(String directory, String envExecution) throws IOException {

		// Effacer les fichiers des répertoires OK et KO
		String envDirectory = envExecution.replace(".", "_").toUpperCase();

		cleanDirectory(directory, envExecution, envDirectory, TraitementEtat.OK);

		cleanDirectory(directory, envExecution, envDirectory, TraitementEtat.KO);

		cleanDirectory(directory, envExecution, envDirectory, TraitementEtat.ENCOURS);

	}

	private static void cleanDirectory(String directory, String envExecution, String envDirectory, TraitementEtat etat)
			throws IOException {
		File f = Paths.get(ApiReceptionService.directoryReceptionEtat(directory, envDirectory, etat)).toFile();
		if (!f.exists()) {
			return;
		}
		File[] fs = f.listFiles();
		for (File z : fs) {
			if (z.isDirectory()) {
				FileUtils.deleteDirectory(z);
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
	 */
	private static boolean deleteIfArchived(String repertoire, String envExecution, File z) {

		String entrepot = ManipString.substringBeforeFirst(z.getName(), "_");
		String filename = ManipString.substringAfterFirst(z.getName(), "_");

		// ajout d'un garde fou : si le fichier n'est pas archivé : pas touche
		File fCheck = Paths
				.get(ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, envExecution, entrepot),
						filename)
				.toFile();

		if (fCheck.exists()) {
			return z.delete();
		} else {
			return z.renameTo(fCheck);
		}
	}

	/**
	 * 
	 * @param envExecution
	 * @throws SQLException
	 */
	private static void resetPending(String envExecution) throws SQLException {
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
	 * @throws SQLException
	 */
	private static void receive(String envExecution, boolean repriseEnCOurs) throws SQLException {
		if (repriseEnCOurs) {
			message("Reprise des fichiers en cours de traitement");
			resetPending(envExecution);
		} else {
			message("Reception de nouveaux fichiers");
			ArcThreadFactory recevoir = new ArcThreadFactory(mapParam, TraitementPhase.RECEPTION);
			recevoir.execute();
			message("Reception : " + recevoir.getReport().nbLines + " e : " + recevoir.getReport().duree + " ms");
		}

	}

	public static void initialize() throws SQLException, ParseException {
		ArcThreadFactory initialiser = new ArcThreadFactory(mapParam, TraitementPhase.INITIALISATION);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");

		String lastInitialize = null;
		lastInitialize = UtilitaireDao.get("arc").getString(null,
				new PreparedStatementBuilder("select last_init from arc.pilotage_batch "));

		Date dNow = new Date();
		Date dLastInitialize;

		dLastInitialize = dateFormat.parse(lastInitialize);

		// la nouvelle initialisation se lance si la date actuelle est postérieure à la
		// date programmée d'initialisation (last_init)
		// on ne la lance que s'il n'y a rien en cours (pas essentiel mais plus
		// sécurisé)
		if ((!dejaEnCours && dLastInitialize.compareTo(dNow) < 0)) {

			message("Initialisation en cours");

			initialiser.execute();

			message("Initialisation terminée : " + (int) initialiser.getReport().nbLines + " e : "
					+ initialiser.getReport().duree + " ms");

			UtilitaireDao.get("arc").executeRequest(null,
					new PreparedStatementBuilder(
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
	 * @throws SQLException
	 * @throws ArcException
	 */
	private HashMap<TraitementPhase, Integer> elligible() throws SQLException, ArcException {
		PreparedStatementBuilder query = new PreparedStatementBuilder();

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
