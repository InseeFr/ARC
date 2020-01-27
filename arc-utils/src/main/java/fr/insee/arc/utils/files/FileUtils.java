package fr.insee.arc.utils.files;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerDispatcher;

public class FileUtils {

    public static final String EXTENSION_ZIP = ".zip";
    public static final String EXTENSION_CSV = ".csv";
    private static final Logger LOGGER = Logger.getLogger(FileUtils.class);
    public static final char SEMICOLON = ';';

    private FileUtils() {
	throw new IllegalStateException("Utility class");
    }

    public static boolean isCompletelyWritten(File file) {
	RandomAccessFile stream = null;
	try {
	    stream = new RandomAccessFile(file, "rw");
	    return true;
	} catch (Exception e) {
		LoggerDispatcher.warn("Le fichier"+ file.getName()+ "est en cours d'�criture.", LOGGER);
	} finally {
	    if (stream != null) {
		try {
		    stream.close();
		} catch (IOException e) {
		}
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

}
