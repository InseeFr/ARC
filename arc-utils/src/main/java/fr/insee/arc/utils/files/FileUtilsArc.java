package fr.insee.arc.utils.files;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	RandomAccessFile stream = null;
	try {
	    stream = new RandomAccessFile(file, "rw");
	    return true;
	} catch (Exception e) {
            LoggerHelper.warnAsComment(LOGGER, e, "Le fichier", file.getName(), "est en cours d'écriture.");
	} finally {
	    if (stream != null) {
		try {
		    stream.close();
                } catch (IOException e) {}
	    }
	}
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    // Silent catch
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
	 * @param directoryToBeDeleted
	 * @return
	 */
	public static boolean deleteDirectory(File directoryToBeDeleted) {
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file);
	        }
	    }
	    return directoryToBeDeleted.delete();
	}
    
}
