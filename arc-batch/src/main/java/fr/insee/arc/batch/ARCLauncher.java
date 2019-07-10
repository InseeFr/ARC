package fr.insee.arc.batch;

import java.io.File;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import fr.insee.arc.core.model.ServiceReporting;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.model.TypeTraitementPhase;
import fr.insee.arc.core.service.AbstractPhaseService;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

/**
 * Launch class for the ARC application 07/08/2015 Version for performance tests
 * and pre-production
 * 
 * @author Manu
 */
public class ARCLauncher {
	private static final Logger LOGGER = Logger.getLogger(ARCLauncher.class);
	static HashMap<String, String> mapParam = new HashMap<>();
	private static final String ENV = "env";
	private static final String REPERTOIRE = "repertoire";
	private static final String NUMLOT = "numlot";
	private static final String ENV_EXECUTION = "envExecution";
	/**
	 * variable dateInitialisation si vide (ou si date du jour+1 depassé à 20h), je
	 * lance initialisation et j'initialise dateInitialisation à la nouvelle date du
	 * jour puis une fois terminé, je lancent la boucle des batchs si date du jour+1
	 * depassé a 20h, - j'indique aux autre batchs de s'arreter - une fois arretés,
	 * je met tempo à la date du jour - je lance initialisation etc.
	 */

	// Production mode (loops on the envelope packets)
	private static boolean production = true;

	// Do wee keep the intermediary data in the database or do we export them as
	// file in the export folder?
	// false in production
	private static boolean keepInDatabase = false;

//	private static String version="ARC-DADS v008 09/05/2018";
	private static String version = "ARC-CTS v008 20/08/2018";

	// For the current batch, the total size of the envelopes processed must not
	// exceed a certain limit
	protected static String maxReceptionSizeInMB = "100";

	// Number of files processed in each phase iteration
	protected static String numberOfFiles = "31";

	// Maximum lag behind phases: if a phase is deltaStepAllowed steps ahead, it
	// waits for the next one to finish before proceeding
	private static int deltaStepAllowed = 10000;

	// Frequency at which the phases are started
	private static int poolingDelay = 1000;

	/**
	 * notificationStart variable pilotage de l'ordonnancement du batch. Voir
	 * modalité pluinitialisations bas 0 initlisation terminée, ok pour passer les
	 * autres batchs 1 demande d'initialisation programmée : les batchs doivent
	 * s'arreter 2 tout les batch sont arretés : pret a initialiser 3 initialisation
	 * lancée
	 **/
	// Nombre de jour entre chaque batch
//	private static int jourEntreInitialisation = 1;
//	// heure de départ min de l'initialisation
//	private static Integer heureInitialisationMin= 20;
//	// heure de fin max de l'initialisation
//	private static Integer heureInitialisationMax= 5;

	// interval entre chaque initialisation en nb de jours
	private final static Integer INTERVAL_JOUR_INITIALISATION = 7;

	public static ArrayList<Integer> step = new ArrayList<Integer>();

	/**
	 * Sleep for a given duration.
	 * 
	 * @param duration Duration in milliseconds.
	 */
	public static void sleep(int duration) {

		try {
			Thread.sleep(duration);
		} catch (InterruptedException ex) {
			LoggerDispatcher.error("sleep()", ex, LOGGER);
			Thread.currentThread().interrupt();
		}
	}

	public static class DateUtil {
		public static Date addDays(Date date, int days) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, days); // A negative number would decrement the days
			return cal.getTime();
		}
	}

	public static class InitializeThread extends Thread {

		public ServiceReporting report = new ServiceReporting(-1, -1);

		@Override
		public void run() {
			InitializeBatch c = new InitializeBatch(mapParam.get(ENV), mapParam.get(ENV_EXECUTION),
					mapParam.get(REPERTOIRE), maxReceptionSizeInMB, keepInDatabase ? null : mapParam.get(NUMLOT));
			c.execute();
			this.report = c.report;
		}
	}

	public static class ReceiveThread extends Thread {

		public ServiceReporting report = new ServiceReporting(-1, -1);

		@Override
		public void run() {
			ReceiveBatch c = new ReceiveBatch(mapParam.get(ENV), mapParam.get(ENV_EXECUTION),
					mapParam.get(REPERTOIRE), maxReceptionSizeInMB, keepInDatabase ? null : mapParam.get(NUMLOT));
			c.execute();
			this.report = c.report;
		}
	}

	public static class LoadThread extends Thread {

		public ServiceReporting report = new ServiceReporting(-1, -1);

		@Override
		public void run() {
			LoadBatch c = new LoadBatch(mapParam.get(ENV), mapParam.get(ENV_EXECUTION), mapParam.get(REPERTOIRE),
					numberOfFiles, keepInDatabase ? null : mapParam.get(NUMLOT));
			c.execute();
			this.report = c.report;
			step.set(step.indexOf(0), 1);
		}
	}

	public static class NormalizeThread extends Thread {

		public ServiceReporting report = new ServiceReporting(-1, -1);

		@Override
		public void run() {
			NormalizeBatch c = new NormalizeBatch(mapParam.get(ENV), mapParam.get(ENV_EXECUTION),
					mapParam.get(REPERTOIRE), numberOfFiles, keepInDatabase ? null : mapParam.get(NUMLOT));
			c.execute();
			step.set(step.indexOf(1), 2);
			this.report = c.report;
		}
	}

	public static class ControlThread extends Thread {

		public ServiceReporting report = new ServiceReporting(-1, -1);

		@Override
		public void run() {
			ControlBatch c = new ControlBatch(mapParam.get(ENV), mapParam.get(ENV_EXECUTION),
					mapParam.get(REPERTOIRE), numberOfFiles, keepInDatabase ? null : mapParam.get(NUMLOT));
			c.execute();
			step.set(step.indexOf(2), 3);
			this.report = c.report;
		}
	}

	public static class FilterThread extends Thread {

		public ServiceReporting report = new ServiceReporting(-1, -1);

		@Override
		public void run() {
			FilterBatch c = new FilterBatch(mapParam.get(ENV), mapParam.get(ENV_EXECUTION), mapParam.get(REPERTOIRE),
					numberOfFiles, keepInDatabase ? null : mapParam.get(NUMLOT));
			c.execute();
			step.set(step.indexOf(3), 4);
			this.report = c.report;
		}
	}

	public static class MapperThread extends Thread {

		public ServiceReporting report = new ServiceReporting(-1, -1);

		@Override
		public void run() {
			MapperBatch c = new MapperBatch(mapParam.get(ENV), mapParam.get(ENV_EXECUTION), mapParam.get(REPERTOIRE),
					numberOfFiles, keepInDatabase ? null : mapParam.get(NUMLOT));
			c.execute();
			step.remove(step.indexOf(4));
			this.report = c.report;
		}
	}

	public static void message(String msg) {
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "  " + msg);
	}

	/**
	 * Main ARC launcher method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		boolean remainingFile = false;
		PropertiesHandler properties = new PropertiesHandler();

		do {

			message("Batch ARC " + version);

			try {
				String env = properties.getBatchArcEnvironment();
				mapParam.put(ENV, env);
				String envExecution = properties.getBatchExecutionEnvironment();
				mapParam.put(ENV_EXECUTION, envExecution);
				String repertoire = properties.getBatchParametersDirectory();
				mapParam.put(REPERTOIRE, repertoire);

				creerTablePilotageBatch();

				InitializeThread initializer = new InitializeThread();
				ReceiveThread receiver = new ReceiveThread();
				LoadThread loader = new LoadThread();
				NormalizeThread normalizer = new NormalizeThread();
				ControlThread controller = new ControlThread();
				FilterThread filter = new FilterThread();
				MapperThread mapper = new MapperThread();

				message("Maintenance pilotage");
				AbstractPhaseService.pilotageMaintenance(null, envExecution, "freeze");
				message("Fin de Maintenance pilotage");

				message("Déplacements de fichiers");

				boolean productionOn = productionOn();

				if (productionOn()) {
					// on vide les repertoire de chargement OK, KO, ENCOURS
					effacerRepertoireChargement(repertoire, envExecution);

					// des archives n'ont elles pas été traitées jusqu'au bout ?
					ArrayList<String> aBouger = new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
							"select distinct container from " + envExecution + ".pilotage_fichier where etape=1"))
									.mapContent().get("container");

					boolean dejaEnCours = (aBouger != null);

					// si oui, on essaie de recopier les archives dans chargement OK
					if (aBouger != null) {

						for (String container : aBouger) {
							String entrepotContainer = ManipString.substringBeforeFirst(container, "_");
							String originalContainer = ManipString.substringAfterFirst(container, "_");

							File fIn = new File(repertoire + envExecution.replace(".", "_").toUpperCase()
									+ File.separator + TypeTraitementPhase.REGISTER + "_" + entrepotContainer
									+ "_ARCHIVE" + File.separator + originalContainer);
							File fOut = new File(repertoire + envExecution.replace(".", "_").toUpperCase()
									+ File.separator + TypeTraitementPhase.REGISTER + "_" + TraitementState.OK
									+ File.separator + container);

							try {
								Files.copy(fIn.toPath(), fOut.toPath());
							} catch (Exception e) {
								message("Fichier " + container + " inexistant dans Archive");
							}
						}
					}

					message("Fin des déplacements de fichiers");

					message("Traitement");

					do {
						step = new ArrayList<Integer>();
						// plage d'initialisation

						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");
						DateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddHH");
						DateFormat dateFormatHour = new SimpleDateFormat("HH");

						String lastInitialize = null;
						lastInitialize = UtilitaireDao.get("arc").getString(null,
								"select last_init from arc.pilotage_batch ");

						Date dNow = new Date();
						Date dLastInitialize;

						mapParam.put(NUMLOT, dateFormat2.format(dNow));

						dLastInitialize = dateFormat.parse(lastInitialize);

						// la nouvelle initialisation se lance directe : pas de plage horaire. Mais
						// juste en cas de modification de regles.
						// on lance toujours l'initialisation en mode "non production"
						// on ne la lance que s'il n'y a rien en cours (pas essentiel mais plus
						// sécurisé)
						if ((!dejaEnCours && dLastInitialize.compareTo(dNow) < 0) || !production) {

							initializer.start();
							if (production) {
								message("Initialisation : " + (int) initializer.report.nbLines + " e : "
										+ initializer.report.duree + " ms");
							}
							ApiInitialisationService.setDummyFilePROD(false);

							UtilitaireDao.get("arc").executeRequest(null,
									"update arc.pilotage_batch set last_init=to_char(to_date(last_init,'yyyy-mm-dd')+interval '"
											+ INTERVAL_JOUR_INITIALISATION + " days','yyyy-mm-dd')||':"
											+ AbstractPhaseService.PRODUCTION_START_TIME
											+ "' , operation=case when operation='R' then 'O' else operation end;");

							// on met la date d'initialsiation à la date courante
							// si la production a demandée à etre réactivée, on la réactive
							// UtilitaireDao.get("arc").executeRequest(null, "update arc.pilotage_batch set
							// last_init='"+dateFormat.format(dNow)+"', operation=case when operation='R'
							// then 'O' else operation end;");
						}
						productionOn = productionOn();

						// Vérifier si la production est activée
						if (productionOn) {

							// Reception : ne faire que si il n'y a rien déjà en cours au début du batch
							if (!dejaEnCours) {
								receiver.start();
								dejaEnCours = false;
							}

							if (production) {
								message("Reception : " + (int) receiver.report.nbLines + " e : " + receiver.report.duree
										+ " ms");
							}

							remainingFile = (int) receiver.report.nbLines > 0;

							// on lance tout systematiquement pour la reprise sur erreur
							step.add(0);
							step.add(1);
							step.add(2);
							step.add(3);
							step.add(4);

							do {

								// System.out.println(step);

								if (!loader.isAlive() && step.contains(0)
										&& Collections.frequency(step, 1) < deltaStepAllowed) {
									loader = new LoadThread();
									loader.start();
								}

								if (!normalizer.isAlive() && step.contains(1)
										&& Collections.frequency(step, 2) < deltaStepAllowed) {
									normalizer = new NormalizeThread();
									normalizer.start();
								}

								if (!controller.isAlive() && step.contains(2)
										&& Collections.frequency(step, 3) < deltaStepAllowed) {
									controller = new ControlThread();
									controller.start();
								}

								if (!filter.isAlive() && step.contains(3)
										&& Collections.frequency(step, 4) < deltaStepAllowed) {
									filter = new FilterThread();
									filter.start();
								}

								if (!mapper.isAlive() && step.contains(4)) {
									mapper = new MapperThread();
									mapper.start();
								}

								sleep(poolingDelay);

								if (loader.report.nbLines > 0) {
									message(step + " Chargement : " + (int) loader.report.nbLines + " l : "
											+ loader.report.duree + " ms");

									// pour la reprise sur erreur : on relance direct quand on a effectivement
									// traité des enregistrements
									if (!step.contains(0)) {
										step.add(0);
									}
									loader.report.nbLines = -1;
								}

								if (normalizer.report.nbLines > 0) {
									message(step + " Normage : " + (int) normalizer.report.nbLines + " l : "
											+ normalizer.report.duree + " ms");
									if (!step.contains(1)) {
										step.add(1);
									}
									normalizer.report.nbLines = -1;
								}

								if (controller.report.nbLines > 0) {
									message(step + " Controle : " + (int) controller.report.nbLines + " l : "
											+ controller.report.duree + " ms");
									if (!step.contains(2)) {
										step.add(2);
									}
									controller.report.nbLines = -1;
								}

								if (filter.report.nbLines > 0) {
									message(step + " Filtre : " + (int) filter.report.nbLines + " l : "
											+ filter.report.duree + " ms");
									if (!step.contains(3)) {
										step.add(3);
									}
									filter.report.nbLines = -1;
								}

								if (mapper.report.nbLines > 0) {
									message(step + " Mapping : " + (int) mapper.report.nbLines + " l : "
											+ mapper.report.duree + " ms");
									if (!step.contains(4)) {
										step.add(4);
									}
									mapper.report.nbLines = -1;
								}

								productionOn = productionOn();

								if (!productionOn) {
									break;
								}
							} while (step.size() > 0);

							if (productionOn) {
								// Effacer les fichiers du répertoire OK
								effacerRepertoireChargement(repertoire, envExecution);
							}
						}

						// si on n'est pas en production, on itere tant qu'il y a des fichiers dans le
						// repertoire.
					} while (!production && receiver.report.nbLines > 0 && productionOn);

					message("Fin");

					if (args != null && args.length > 0 && args[0].equals("noExit")) {
						message("No Exit");
					} else {
						System.exit(0);
					}
				}

			} catch (Exception ex) {
				LoggerHelper.errorGenTextAsComment(ARCLauncher.class, "main()", LOGGER, ex);
				System.exit(202);
			}

		} while (remainingFile);

		message("Fin du batch");

	}

	/**
	 * Créer la table de pilotage batch si elle n'existe pas déjà
	 * 
	 * @throws Exception
	 */
	public static void creerTablePilotageBatch() throws Exception {
		StringBuilder requete = new StringBuilder();
		requete.append(
				"\n CREATE TABLE IF NOT EXISTS arc.pilotage_batch (last_init text collate \"C\", operation text collate \"C\"); ");
		requete.append(
				"\n insert into arc.pilotage_batch select '1900-01-01:00','O' where not exists (select 1 from arc.pilotage_batch); ");
		UtilitaireDao.get("arc").executeRequest(null, requete);
	}

	/**
	 * test si la chaine batch est arrétée
	 * 
	 * @return
	 * @throws Exception
	 */
	public static boolean productionOn() throws Exception {
		return UtilitaireDao.get("arc").hasResults(null, "select 1 from arc.pilotage_batch where operation='O'");
	}

	/**
	 * Effacer les répertoires de chargement OK KO et ENCOURS
	 * 
	 * @param repertoire
	 * @param envExecution
	 * @throws Exception
	 */
	public static void effacerRepertoireChargement(String repertoire, String envExecution) throws Exception {

		// Effacer les fichiers des répertoires OK et KO
		File f = new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator
				+ TypeTraitementPhase.REGISTER + "_" + TraitementState.OK);
		File[] fs = f.listFiles();
		for (File z : fs) {
			if (z.isDirectory()) {
				FileUtils.deleteDirectory(z);
			} else {
				// ajout d'un garde fou : si le fichier n'est pas archivé : pas touche
				File fCheck = new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator
						+ TypeTraitementPhase.REGISTER + "_" + ManipString.substringBeforeFirst(z.getName(), "_")
						+ "_ARCHIVE" + File.separator + ManipString.substringAfterFirst(z.getName(), "_"));
				if (fCheck.exists()) {
					z.delete();
				} else {
					z.renameTo(fCheck);
				}
			}
		}

		// Effacer les fichiers du répertoire KO
		f = new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator
				+ TypeTraitementPhase.REGISTER + "_" + TraitementState.KO);
		fs = f.listFiles();
		for (File z : fs) {
			// ajout d'un garde fou : si le fichier n'est pas archivé : pas touche
			File fCheck = new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator
					+ TypeTraitementPhase.REGISTER + "_" + ManipString.substringBeforeFirst(z.getName(), "_")
					+ "_ARCHIVE" + File.separator + ManipString.substringAfterFirst(z.getName(), "_"));
			if (fCheck.exists()) {
				z.delete();
			} else {
				z.renameTo(fCheck);
			}
		}

		f = new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator
				+ TypeTraitementPhase.REGISTER + "_" + TraitementState.ENCOURS);
		fs = f.listFiles();
		for (File z : fs) {
			// ajout d'un garde fou : si le fichier n'est pas archivé : pas touche
			File fCheck = new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator
					+ TypeTraitementPhase.REGISTER + "_" + ManipString.substringBeforeFirst(z.getName(), "_")
					+ "_ARCHIVE" + File.separator + ManipString.substringAfterFirst(z.getName(), "_"));
			if (fCheck.exists()) {
				z.delete();
			} else {
				z.renameTo(fCheck);
			}
		}
	}
}
