package fr.insee.arc.utils.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.LoggerHelper;

public class FileUtilsArc {

	public static final String EXTENSION_ZIP = ".zip";
	public static final String EXTENSION_CSV = ".csv";
	private static final Logger LOGGER = LogManager.getLogger(FileUtilsArc.class);
	public static final char SEMICOLON = ';';

	private FileUtilsArc() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isCompletelyWritten(File file) {
		try (RandomAccessFile stream = new RandomAccessFile(file, "rw");) {
			return true;
		} catch (IOException e) {
			LoggerHelper.warnAsComment(LOGGER, e, "Le fichier", file.getName(), " est en cours d'écriture");
		}
		return false;
	}

	/**
	 * Copie le fichier de chemin {@code cheminFichierSource} dans le fichier de
	 * chemin {@code cheminFichierCible}. Le chemin du fichier cible est créé
	 * dynamiquement.
	 *
	 * @param cheminFichierSource
	 * @param cheminFichierCible
	 * @param options
	 * @throws IOException
	 */
	public static void copy(Path cheminFichierSource, Path cheminFichierCible, CopyOption... options)
			throws IOException {
		mkDirs(cheminFichierCible.getParent());
		Files.copy(cheminFichierSource, cheminFichierCible, options);
	}

	/**
	 * Déplace le fichier de chemin {@code cheminFichierSource} dans le fichier de
	 * chemin {@code cheminFichierCible}. Le chemin du fichier cible est créé
	 * dynamiquement.
	 *
	 * @param cheminFichierSource
	 * @param cheminFichierCible
	 * @param options
	 * @throws IOException
	 */
	public static void move(Path cheminFichierSource, Path cheminFichierCible, CopyOption... options)
			throws IOException {
		mkDirs(cheminFichierCible.getParent());
		Files.move(cheminFichierSource, cheminFichierCible, options);
	}

	/**
	 * Crée récursivement l'arborescence de répertoires {@code aPath}.
	 *
	 * @param aPath
	 * @throws IOException
	 */
	public static void mkDirs(Path aPath) throws IOException {
		if (!aPath.getParent().toFile().exists()) {
			mkDirs(aPath.getParent());
		}
		if (!aPath.toFile().exists()) {
			Files.createDirectory(aPath);
		}
	}

	/**
	 * delete directory recursively
	 * 
	 * @param directoryToBeDeleted
	 * @return
	 * @throws IOException 
	 */
	public static void deleteDirectory(File directoryToBeDeleted) throws ArcException {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		delete(directoryToBeDeleted);
	}
	

	/**
	 * delete and recreate directory
	 * @param directoryToBeDeletedAndRecreated
	 * @throws ArcException
	 */
	public static void deleteAndRecreateDirectory(File directoryToBeDeletedAndRecreated) throws ArcException {
		if (directoryToBeDeletedAndRecreated.exists())
		{
			deleteDirectory(directoryToBeDeletedAndRecreated);			
		}
		directoryToBeDeletedAndRecreated.mkdir();
	}

	/**
	 * Rename fileInput into fileOutput. Send a log is rename wasn't successful
	 * 
	 * @param fileInput
	 * @param fileOutput
	 * @return
	 * @throws IOException 
	 */
	public static void renameTo(File fileInput, File fileOutput) throws ArcException {
		boolean renameResult = fileInput.renameTo(fileOutput);

		if (!renameResult) {
			throw new ArcException(ArcExceptionMessage.FILE_RENAME_FAILED,fileInput.getName(),fileOutput.getName());
		}
	}

	/**
	 * Delete fileInput. Send a log is rename wasn't successful
	 * 
	 * @param fileInput
	 * @return
	 * @throws IOException 
	 */
	public static void delete(File fileInput) throws ArcException {
		boolean deleteResult = fileInput.delete();

		if (!deleteResult) {
			throw new ArcException(ArcExceptionMessage.FILE_DELETE_FAILED, fileInput.getName());
		}
	}

}
