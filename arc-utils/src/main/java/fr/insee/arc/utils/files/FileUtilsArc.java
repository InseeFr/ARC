package fr.insee.arc.utils.files;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.LoggerHelper;

public class FileUtilsArc {

	private static final Logger LOGGER = LogManager.getLogger(FileUtilsArc.class);
	
	public static final String EXTENSION_ZIP = ".zip";
	public static final String EXTENSION_CSV = ".csv";

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
	 * Create a directory if not exists
	 * @param f
	 */
	public static void createDirIfNotexist(File directoryToCreate) {
		if (!directoryToCreate.exists()) {
			if (!directoryToCreate.mkdirs())
			{
				LoggerHelper.error(LOGGER, "Le répertoire ", directoryToCreate.getAbsolutePath(), " n'a pas pu être créé");
			}
		}
	}

	/**
	 * Create a directory if not exists
	 * @param f
	 */
	public static void createDirIfNotexist(String fPath) {
		createDirIfNotexist(new File(fPath));
	}

	public static void deleteDirectory(String directoryToBeDeleted) throws ArcException {
		deleteDirectory(new File(directoryToBeDeleted));
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
			throw new ArcException(ArcExceptionMessage.FILE_RENAME_FAILED,fileInput.getAbsolutePath(),fileOutput.getAbsolutePath());
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
			throw new ArcException(ArcExceptionMessage.FILE_DELETE_FAILED, fileInput.getAbsolutePath());
		}
	}
	
	/**
	 * Deplacer un fichier d'un repertoire source vers répertoire cible (pas de
	 * slash en fin du nom de repertoire) Si le fichier existe déjà, il est écrasé
	 *
	 * @param dirIn    , répertoire en entrée, pas de slash à la fin
	 * @param dirOut   , répertoire en sortie, pas de slash à la fin
	 * @param FileName , nom du fichier
	 * @throws ArcException
	 */
	public static void deplacerFichier(String dirIn, String dirOut, String fileNameIn, String fileNameOut)
			throws ArcException {
		if (!dirIn.equals(dirOut)) {
			File fileIn = new File(dirIn + File.separator + fileNameIn);
			File fileOut = new File(dirOut + File.separator + fileNameOut);
			if (fileOut.exists()) {
				FileUtilsArc.delete(fileOut);
			}
			FileUtilsArc.renameTo(fileIn, fileOut);
		}
	}

}
