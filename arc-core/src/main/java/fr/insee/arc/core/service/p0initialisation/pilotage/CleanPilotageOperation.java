package fr.insee.arc.core.service.p0initialisation.pilotage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Integers;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.DataStorage;
import fr.insee.arc.core.service.p0initialisation.pilotage.dao.CleanPilotageDao;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.consumer.ThrowingRunnable;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.LoggerHelper;

/**
 * Remove deprecated files from a target sandbox Deprecated files are the one
 * which had been already retrieved by the client applications since a
 * numberOfDaysToKeepFiles days (retention period) Files retrieved are marked in
 * pilotage in columns called "client"
 * 
 * @author FY2QEQ
 *
 */
public class CleanPilotageOperation {

	private static final Logger LOGGER = LogManager.getLogger(CleanPilotageOperation.class);

	public CleanPilotageOperation(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
		this.cleanPilotageDao = new CleanPilotageDao(sandbox);
	}

	private Sandbox sandbox;
	private CleanPilotageDao cleanPilotageDao;

	private Integer numberOfDaysToKeepData;
	private Integer numberOfFilesToProceed;
	private String rootDirectory;
	private Calendar minDateForFileToBeKept;
	private Integer fileRetentionPeriodInDays;

	/**
	 * Suppression dans la table de pilotage des fichiers consommés 1- une copie des
	 * données du fichier doit avoir été récupérée par tous les clients décalrés 2-
	 * pour un fichier donné, l'ancienneté de son dernier transfert doit dépasser
	 * Nb_Jour_A_Conserver jours RG2.
	 *
	 * @param targetSandbox.getConnection()
	 * @param tablePil
	 * @param tablePil
	 * @throws ArcException
	 */
	public void execute() throws ArcException {
		LoggerHelper.info(LOGGER, "Archivage Début");

		// retrieve parameters
		retrieveOperationParameters();

		// create the temporary table containing files to be deleted from database
		materializeFilesToBeDeletedFromDatabase();

		// delete deprecated data from database and archives files in old directory
		moveDeletedArchivesToArchiveDirectory(deleteArchivesFromDatabase());

		// y'a-t-il des fichiers ou répertoire à supprimer dans le répertoire OLD/ ?
		executeIfFileRetentionPeriodDefined(this::deletedArchivesIfTooOld);

		LoggerHelper.info(LOGGER, "Archivage Fin");

	}

	private void executeIfFileRetentionPeriodDefined(ThrowingRunnable method) throws ArcException {
		if (minDateForFileToBeKept == null) {
			LoggerHelper.info(LOGGER,
					"File retention is not defined. Archive files are not deleted from OLD directory.");
			return;
		} else {
			LoggerHelper.info(LOGGER, "File retention period is defined to "+fileRetentionPeriodInDays+" days. Files with modified date before "+ minDateForFileToBeKept.getTime() +" will be deleted from OLD directory.");
		}
		method.run();
	}

	private void retrieveOperationParameters() {

		BDParameters bdParameters = new BDParameters(ArcDatabase.COORDINATOR);

		// indique combien de jour doivent etre conservé les données apres avoir été
		// récupérées
		this.numberOfDaysToKeepData = bdParameters.getInt(sandbox.getConnection(),
				"ApiInitialisationService.Nb_Jour_A_Conserver", 365);

		// nombre de fichier à traiter lors à chaque itération d'archivage
		this.numberOfFilesToProceed = bdParameters.getInt(sandbox.getConnection(),
				"ApiInitialisationService.NB_FICHIER_PER_ARCHIVE", 10000);

		PropertiesHandler properties = PropertiesHandler.getInstance();

		// root directory of arc filesystem
		this.rootDirectory = properties.getBatchParametersDirectory();

		// rention period of archives files located in OLD directory
		this.fileRetentionPeriodInDays = properties.getFilesRetentionDays();

		// date en dessous de laquelle les fichiers vont être effacés
		
		if (this.fileRetentionPeriodInDays == null)
		{
			this.minDateForFileToBeKept = null;
		}
		else
		{
			this.minDateForFileToBeKept = Calendar.getInstance();
			// remove the fileRetentionPeriodInDays from current date to get the minDateForFileToBeKept
			this.minDateForFileToBeKept.setTime(DateUtils.addDays(new Date(),-fileRetentionPeriodInDays));
		}
		
	}

	/**
	 * Delete the archive from the database and record their filename and location
	 * 
	 * @param numberOfFilesToProceed
	 * @return
	 * @throws ArcException
	 */
	private Map<String, List<String>> deleteArchivesFromDatabase() throws ArcException {
		// initialisation de la liste contenant les archives à déplacer
		Map<String, List<String>> recordedArchives = new HashMap<>();
		recordedArchives.put(ColumnEnum.ENTREPOT.getColumnName(), new ArrayList<>());
		recordedArchives.put(ColumnEnum.NOM_ARCHIVE.getColumnName(), new ArrayList<>());

		Map<String, List<String>> listOfDeletedArchives;

		// on selectionne les fichiers éligibles et on limite le nombre de retour
		// pour que l'update ne soit pas trop massif (perf)
		// on continue jusqu'a ce qu'on ne trouve plus rien à effacer
		do {
			// récupérer le résultat de la requete
			LoggerHelper.info(LOGGER, "Archivage de " + numberOfFilesToProceed + " fichiers - Début");
			listOfDeletedArchives = cleanPilotageDao
					.execQueryDeleteDeprecatedFilesAndSelectArchives(numberOfFilesToProceed);

			// ajouter à la liste recordedArchives les enregistrements de
			// listOfDeletedArchives qui n'existent pas déjà dans recordedArchives
			keepTrackOfDeletedArchives(listOfDeletedArchives, recordedArchives);

			LoggerHelper.info(LOGGER, "Archivage de " + numberOfFilesToProceed + " fichiers - Fin");

		} while (cleanPilotageDao.execQueryIsStillSomethingToDelete());
		return recordedArchives;
	}

	/**
	 * Materialize the file to be deleted from database
	 * 
	 * @param numberOfDaysToKeepData : number of days the file data can be kept in
	 *                               the database after all clients had retrieved
	 *                               them
	 * @throws ArcException
	 */
	private void materializeFilesToBeDeletedFromDatabase() throws ArcException {
		// materialized the file to be deleted
		cleanPilotageDao.execQueryMaterializeFilesToDelete(numberOfDaysToKeepData);
	}

	/**
	 * Delete the archives from the file system if they are too old according to the
	 * files retention period
	 * 
	 * @param rootDirectory
	 * @param fileRetentionPeriodInDays
	 * @throws ArcException
	 */
	protected void deletedArchivesIfTooOld() throws ArcException {
		List<String> entrepots = DataStorage.execQuerySelectEntrepots(sandbox.getConnection());
		// scan des entrepots
		deletedArchivesIfTooOld(entrepots);
	}

	/**
	 * Delete too old archives in the entrepots
	 * @param entrepots
	 * @throws ArcException
	 */
	protected void deletedArchivesIfTooOld(List<String> entrepots) throws ArcException {

		for (String entrepot : entrepots) {

			deletedArchivesIfTooOld(entrepot);

		}

	}

	/**
	 * Delete too old archives in an entrepot
	 * @param entrepot
	 * @throws ArcException
	 */
	protected void deletedArchivesIfTooOld(String entrepot) throws ArcException {

		File dirOut = new File(
				DirectoryPath.directoryReceptionEntrepotArchiveOld(rootDirectory, this.sandbox.getSchema(), entrepot));

		if (!dirOut.exists()) {
			return;
		}

		File[] yearDirectories = dirOut.listFiles(f -> f.isDirectory() && isValidYearDirectory(f));

		deleteFilesInYearDirectory(yearDirectories);
	}

	/**
	 * delete files in year directory
	 * 
	 * @param directoriesToScan
	 * @throws ArcException
	 */
	private void deleteFilesInYearDirectory(File[] directoriesToScan) throws ArcException {
		for (File yearDirectory : directoriesToScan) {
			int yearDirectoryName = Integers.parseInt(yearDirectory.getName());

			// if directory year is strictly too old as compared to yearThreshold, delete
			// the whole directory
			if (yearDirectoryName < this.minDateForFileToBeKept.get(Calendar.YEAR)) {
				LoggerHelper.info(LOGGER, "DELETE old directory "+ yearDirectory.getAbsolutePath());
				FileUtilsArc.deleteDirectory(yearDirectory);
			}
			
			if (yearDirectoryName == this.minDateForFileToBeKept.get(Calendar.YEAR)) {
				for (File fileInDirYearThreshold : yearDirectory.listFiles()) {
					if (minDateForFileToBeKept.getTimeInMillis() > fileInDirYearThreshold.lastModified()) {
						LoggerHelper.info(LOGGER, "DELETE old archive file "+fileInDirYearThreshold.getAbsolutePath());
						FileUtilsArc.delete(fileInDirYearThreshold);
					}
				}
			}
		}
	}

	private void keepTrackOfDeletedArchives(Map<String, List<String>> listOfDeletedArchives,
			Map<String, List<String>> recordedArchives) {
		if (listOfDeletedArchives.isEmpty()) {
			return;
		}

		for (int k = 0; k < listOfDeletedArchives.get(ColumnEnum.ENTREPOT.getColumnName()).size(); k++) {
			boolean toInsert = true;

			// vérifier en parcourant m si on doit réaliser l'insertion
			for (int l = 0; l < recordedArchives.get(ColumnEnum.ENTREPOT.getColumnName()).size(); l++) {
				if (listOfDeletedArchives.get(ColumnEnum.ENTREPOT.getColumnName()).get(k)
						.equals(recordedArchives.get(ColumnEnum.ENTREPOT.getColumnName()).get(l))
						&& listOfDeletedArchives.get(ColumnEnum.NOM_ARCHIVE.getColumnName()).get(k)
								.equals(recordedArchives.get(ColumnEnum.NOM_ARCHIVE.getColumnName()).get(l))) {
					toInsert = false;
					break;
				}
			}

			// si l'enreigstrement de listOfDeletedArchives n'est pas retrouvé dans
			// archivesToBeMoved
			// on l'insere pour le traiter à posteriori
			if (toInsert) {
				recordedArchives.get(ColumnEnum.ENTREPOT.getColumnName())
						.add(listOfDeletedArchives.get(ColumnEnum.ENTREPOT.getColumnName()).get(k));
				recordedArchives.get(ColumnEnum.NOM_ARCHIVE.getColumnName())
						.add(listOfDeletedArchives.get(ColumnEnum.NOM_ARCHIVE.getColumnName()).get(k));
			}
		}
	}

	/**
	 * The deprecated archive files that had been deleted must be moved to the
	 * archivage directory The archivage directory is called "old" and timestamped
	 * 
	 * @param recordedArchives
	 * @throws ArcException
	 */
	private void moveDeletedArchivesToArchiveDirectory(Map<String, List<String>> recordedArchives) throws ArcException {
		if (recordedArchives.get(ColumnEnum.ENTREPOT.getColumnName()).isEmpty()) {
			return;
		}

		// 7. Déplacer les archives effacées dans le répertoire de sauvegarde "OLD"
		PropertiesHandler properties = PropertiesHandler.getInstance();
		String repertoire = properties.getBatchParametersDirectory();

		String entrepotSav = "";
		for (int i = 0; i < recordedArchives.get(ColumnEnum.ENTREPOT.getColumnName()).size(); i++) {
			String entrepot = recordedArchives.get(ColumnEnum.ENTREPOT.getColumnName()).get(i);
			String archive = recordedArchives.get(ColumnEnum.NOM_ARCHIVE.getColumnName()).get(i);
			String dirIn = DirectoryPath.directoryReceptionEntrepotArchive(repertoire, this.sandbox.getSchema(),
					entrepot);
			String dirOut = DirectoryPath.directoryReceptionEntrepotArchiveOldYearStamped(repertoire,
					this.sandbox.getSchema(), entrepot);

			// création du répertoire "OLD" s'il n'existe pas
			if (!entrepotSav.equals(entrepot)) {
				File f = new File(dirOut);
				FileUtilsArc.createDirIfNotexist(f);
				entrepotSav = entrepot;
			}

			// déplacement de l'archive de dirIn vers dirOut
			FileUtilsArc.deplacerFichier(dirIn, dirOut, archive, archive);
		}

		cleanPilotageDao.execQueryMaintenancePilotage();
	}

	private static boolean isValidYearDirectory(File directoryName) {
		return StringUtils.isNumeric(directoryName.getName());
	}

	public CleanPilotageDao getCleanPilotageDao() {
		return cleanPilotageDao;
	}

	public void setCleanPilotageDao(CleanPilotageDao cleanPilotageDao) {
		this.cleanPilotageDao = cleanPilotageDao;
	}

}
