package fr.insee.arc.core.service.p1reception.registerarchive;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.FilesDescriber;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.GzReader;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.TgzReader;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.ZipReader;
import fr.insee.arc.core.service.p1reception.registerarchive.dao.DirectoriesDao;
import fr.insee.arc.core.service.p1reception.registerarchive.dao.MoveFilesToRegisterDao;
import fr.insee.arc.core.service.p1reception.registerarchive.operation.ArchiveCheckOperation;
import fr.insee.arc.core.service.p1reception.registerarchive.operation.ReworkArchiveOperation;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressionExtension;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.utils.ManipString;

public class ArchiveRegistration {

	private static final Logger LOGGER = LogManager.getLogger(ArchiveRegistration.class);

	private Sandbox sandbox;

	private DirectoriesDao directories;

	private FilesDescriber selectedArchives;

	// max total size of selected files
	private int fileSizeLimit;
	// max total number of selected files
	private int maxNumberOfFiles;

	public ArchiveRegistration(Sandbox sandbox, int fileSizeLimit, int maxNumberOfFiles) {
		super();
		this.sandbox = sandbox;
		this.fileSizeLimit = fileSizeLimit;
		this.maxNumberOfFiles = maxNumberOfFiles;
		this.directories = new DirectoriesDao(sandbox);
	}

	// current size of files registered
	private int fileSize = 0;
	// current number of files registered
	private int fileNb = 0;

	/**
	 * Initialize the application directories if needed List the the files received
	 * and start to move to the processing directory
	 * 
	 * @param fileSizeLimit
	 * @param maxNumberOfFiles
	 * @return
	 * @throws ArcException
	 */
	public FilesDescriber moveAndCheckClientFiles() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "moveAndCheckClientFiles");

		// create and register sandbox directories
		directories.createSandboxDirectories();

		this.selectedArchives = new FilesDescriber();

		List<String> entrepotList = MoveFilesToRegisterDao.execQuerySelectDatawarehouses(sandbox.getConnection());

		if (!entrepotList.isEmpty()) {
			moveAndCheckUntilLimit(entrepotList);
		}

		return selectedArchives;
	}

	/**
	 * Moves files into RECEPTION_ENCOURS directory, check if the archives are
	 * readable and returns a description of the content of all treated files.
	 */
	private void moveAndCheckUntilLimit(List<String> entrepotIdList) throws ArcException {

		StaticLoggerDispatcher.info(LOGGER, "Taille limite de fichiers à charger : " + fileSizeLimit);

		for (String entrepot : entrepotIdList) {

			if (isFileRegisteringFinished()) {
				break;
			}

			// create and register datawarehouse sandbox directories (entrepot)
			directories.createSandboxDatawarehouseDirectories(entrepot);

			selectFilesInDatawarehouse(entrepot);
		}

	}

	/**
	 * Check condition to end the file registering Condition is checked when the
	 * size of the files selected or the number of files selected exceed the given
	 * limits
	 * 
	 * @return
	 */
	private boolean isFileRegisteringFinished() {
		return (fileSize > fileSizeLimit || fileNb > maxNumberOfFiles);
	}

	private void selectFilesInDatawarehouse(String entrepot) throws ArcException {

		File fDirIn = new File(directories.getDirEntrepotIn());
		// vérifier le type (répertoire)
		if (fDirIn.isDirectory()) {

			File[] filesDirIn = fDirIn.listFiles();

			// trier par nom
			Arrays.sort(filesDirIn, (f1, f2) -> f1.getName().compareTo(f2.getName()));

			for (File f : filesDirIn) {

				// ignorer le fichier s'il est en cours d'ecriture
				if (!FileUtilsArc.isCompletelyWritten(f)) {
					continue;
				}

				if (isFileRegisteringFinished()) {
					break;
				}

				ReworkArchiveOperation reworkInstance = new ReworkArchiveOperation(directories, entrepot, f);

				reworkInstance.qualifyAndRename();

				reworkInstance.reworkArchive();

				this.fileSize += reworkInstance.getReworkedArchiveSize();

				FilesDescriber selectedArchive = checkArchiveFiles(reworkInstance.getReworkedArchiveFile());

				selectedArchives.addAll(selectedArchive);

				fileNb = fileNb + selectedArchive.getFilesAttribute().size();

				// enregistrer le fichier
				
				MoveFilesToRegisterDao.registerArchive(sandbox, entrepot, reworkInstance.getReworkedArchiveName());

			}
		}
	}

	/**
	 * Select some archive, check them and return a report as FilesDescribers
	 *
	 * @param filesIn the archives
	 * @return a GenericBean describing the archive
	 * @throws ArcException
	 */
	private FilesDescriber checkArchiveFiles(File f) throws ArcException {

		FilesDescriber content = new FilesDescriber();

		String entrepot = ManipString.substringBeforeFirst(f.getName(), "_") + "_";

		if (f.getName().endsWith(CompressionExtension.TAR_GZ.getFileExtension())
				|| f.getName().endsWith(CompressionExtension.TGZ.getFileExtension())) {
			content.addAll(new ArchiveCheckOperation(new TgzReader()).checkArchive(f, entrepot));
		} else if (f.getName().endsWith(CompressionExtension.ZIP.getFileExtension())) {
			content.addAll(new ArchiveCheckOperation(new ZipReader()).checkArchive(f, entrepot));
		} else if (f.getName().endsWith(CompressionExtension.GZ.getFileExtension())) {
			content.addAll(new ArchiveCheckOperation(new GzReader()).checkArchive(f, entrepot));
		} else {
			throw new ArcException(ArcExceptionMessage.INVALID_FILE_FORMAT);
		}

		return content;
	}

	public int getFileNb() {
		return fileNb;
	}
	
	
}
