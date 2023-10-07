package fr.insee.arc.core.service.p0initialisation.useroperation;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p0initialisation.useroperation.dao.ReplayOrDeleteFilesDao;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

public class ReplayOrDeleteFilesOperation {

	private static final Logger LOGGER = LogManager.getLogger(ReplayOrDeleteFilesOperation.class);

	public ReplayOrDeleteFilesOperation(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
		this.replayOrDeleteFilesDao = new ReplayOrDeleteFilesDao(sandbox);
	}
	
	private Sandbox sandbox;
	
	private ReplayOrDeleteFilesDao replayOrDeleteFilesDao;
	
	
	public void processMarkedFiles() throws ArcException
	{
		replayMarkedFiles();
		
		deleteMarkedFiles();
	}
	
	
	/**
	 * Méthode pour rejouer des fichiers
	 *
	 * @param connexion
	 * @param tablePil
	 * @throws ArcException
	 */
	public void replayMarkedFiles() throws ArcException {
		LoggerHelper.info(LOGGER, "reinstateWithRename");

		String envExecution=sandbox.getSchema();
		
		// on cherche tous les containers contenant un fichier à rejouer
		// on remet l'archive à la racine
		
		List<String> containerList = replayOrDeleteFilesDao.execQuerySelectArchiveToReplay();
		
		if (!containerList.isEmpty()) {
			PropertiesHandler properties = PropertiesHandler.getInstance();
			String repertoire = properties.getBatchParametersDirectory();

			for (String s : containerList) {

				String entrepot = ManipString.substringBeforeFirst(s, "_");
				String archive = ManipString.substringAfterFirst(s, "_");

				String dirIn = DirectoryPath.directoryReceptionEntrepotArchive(repertoire, envExecution, entrepot);
				String dirOut = DirectoryPath.directoryReceptionEntrepot(repertoire, envExecution, entrepot);

				FileUtilsArc.deplacerFichier(dirIn, dirOut, archive, archive);

			}

		}

		replayOrDeleteFilesDao.execQueryDeleteArchiveToReplay();
	}

	/**
	 * Suppression dans la table de pilotage des fichiers qui ont été marqué par la
	 * MOA (via la colonne to_delete de la table de pilotage);
	 *
	 * @param connexion
	 * @param tablePil
	 * @throws ArcException
	 */
	private void deleteMarkedFiles() throws ArcException {
		LoggerHelper.info(LOGGER, "Delete file marked by user as to be deleted");
		
		replayOrDeleteFilesDao.execQueryDeleteFileToDelete();
	}


	
	
}
