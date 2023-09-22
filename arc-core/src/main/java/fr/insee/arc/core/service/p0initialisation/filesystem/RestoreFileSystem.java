package fr.insee.arc.core.service.p0initialisation.filesystem;

import java.io.File;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.global.dao.DataStorage;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;
import fr.insee.arc.core.service.p1reception.ApiReceptionService;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

public class RestoreFileSystem {

	private static final Logger LOGGER = LogManager.getLogger(RestoreFileSystem.class);

	private Connection connection;
	private String envExecution;
	
	
	
	public RestoreFileSystem(Connection connection, String envExecution) {
		super();
		this.connection = connection;
		this.envExecution = envExecution;
	}


	/**
	 * remet le filesystem en etat en cas de restauration de la base
	 *
	 * @throws ArcException
	 */
	public void execute() throws ArcException {
		LoggerHelper.info(LOGGER, "Reconstruction du filesystem");

		// parcourir toutes les archives dans le répertoire d'archive
		String rootDirectory = PropertiesHandler.getInstance().getBatchParametersDirectory();
		FileUtilsArc.createDirIfNotexist(FileSystemManagement.directoryEnvRoot(rootDirectory, envExecution));

		// pour chaque entrepot de données,
		// Comparer les archives du répertoire aux archives enregistrées dans la table
		// d'archive :
		// comme la table d'archive serait dans l'ancien état de données
		// on peut remettre dans le repertoire de reception les archives qu'on ne
		// retrouvent pas dans la table

		List<String> entrepotList = DataStorage.execQuerySelectDatastorage(connection);
		
		for (String entrepot : entrepotList) {
			rebuildFileSystemInEntrepot(rootDirectory, entrepot);
		}
	}
	
	
	private void rebuildFileSystemInEntrepot(String rootDirectory, String entrepot) throws ArcException
	{
		String dirEntrepotArchive = ApiReceptionService.directoryReceptionEntrepotArchive(rootDirectory, envExecution,
		entrepot);
		String dirEntrepot = ApiReceptionService.directoryReceptionEntrepot(rootDirectory, envExecution, entrepot);

		FileUtilsArc.createDirIfNotexist(dirEntrepotArchive);
		FileUtilsArc.createDirIfNotexist(dirEntrepot);
		
		// On cherche les fichiers du répertoire d'archive qui ne sont pas dans la table
		// archive
		// Si on en trouve ce n'est pas cohérent et on doit remettre ces fichiers dans
		// le répertoire de reception
		// pour être rechargés				
		List<File> dirEntrepotArchiveFiles = Arrays.asList(new File(dirEntrepotArchive).listFiles());
		// on les insere dans une table temporaires t_files
		DataStorage.execQueryRegisterFilesInDatabase(connection, dirEntrepotArchiveFiles);

		List<String> fileToBeMoved = DataStorage.execQuerySelectFilesNotInRegisteredArchives(connection, envExecution);
		for (String fname : fileToBeMoved) {
			ApiReceptionService.deplacerFichier(dirEntrepotArchive, dirEntrepot, fname, fname);
		}
		
		moveBackNotRegisteredFilesFromEntrepotArchiveToEntrepot(dirEntrepot, dirEntrepotArchive);
		
		manageDuplicateArchives(dirEntrepot);
		
	}
	
	/**
	 * Remettre en chargement les archives non enregistrées dans la base
	 * Utile en cas de crash pour remettre les fichiers au bon endroit automatiquement
	 * @param dirEntrepot
	 * @param dirEntrepotArchive
	 * @throws ArcException
	 */
	private void moveBackNotRegisteredFilesFromEntrepotArchiveToEntrepot(String dirEntrepot, String dirEntrepotArchive) throws ArcException
	{
		// On cherche les fichiers du répertoire d'archive qui ne sont pas dans la table
		// archive
		// Si on en trouve ce n'est pas cohérent et on doit remettre ces fichiers dans
		// le répertoire de reception
		// pour être rechargés				
		List<File> dirEntrepotArchiveFiles = Arrays.asList(new File(dirEntrepotArchive).listFiles());
		// on les insere dans une table temporaires t_files
		DataStorage.execQueryRegisterFilesInDatabase(connection, dirEntrepotArchiveFiles);

		List<String> fileToBeMoved = DataStorage.execQuerySelectFilesNotInRegisteredArchives(connection, envExecution);
		for (String fname : fileToBeMoved) {
			ApiReceptionService.deplacerFichier(dirEntrepotArchive, dirEntrepot, fname, fname);
		}
	}
	
	/**
	 * Effacer les archives identiques avec le nom sans # ou un numéro # inférieur
	 * @param dirEntrepot
	 * @throws ArcException
	 */
	private void manageDuplicateArchives(String dirEntrepot) throws ArcException
	{
		// Traitement des # dans le repertoire de reception
		// on efface les # dont le fichier existe déjà avec un autre nom sans # ou un
		// numéro # inférieur

		List<File> dirEntrepotFiles = Arrays.asList(new File(dirEntrepot).listFiles());

		for (File fichier : dirEntrepotFiles) {
			String filenameWithoutExtension = ManipString.substringBeforeFirst(fichier.getName(), ".");
			String ext = "." + ManipString.substringAfterFirst(fichier.getName(), ".");

			if (filenameWithoutExtension.contains("#")) {
				Integer number = ManipString
						.parseInteger(ManipString.substringAfterLast(filenameWithoutExtension, "#"));

				// c'est un fichier marqué
				if (number != null) {

					String originalIdSource = ManipString.substringBeforeLast(filenameWithoutExtension, "#");

					// tester ce qu'on doit en faire

					// comparer au fichier sans index
					File fichierDeReference;
					
					fichierDeReference = new File(dirEntrepot + File.separator + originalIdSource + ext);
					FileSystemManagement.deleteFileIfSameAs(fichier, fichierDeReference);

					// comparer aux fichier avec un index précédent
					for (int i = 2; i < number; i++) {
						fichierDeReference = new File(dirEntrepot + File.separator + originalIdSource + "#" + i + ext);
						FileSystemManagement.deleteFileIfSameAs(fichier, fichierDeReference);
					}

				}
			}

		}
	}
	
}
