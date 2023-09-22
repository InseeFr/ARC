package fr.insee.arc.core.service.p0initialisation.useroperation;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p0initialisation.useroperation.dao.ReplayOrDeleteFilesDao;
import fr.insee.arc.core.service.p1reception.ApiReceptionService;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

public class ReplayOrDeleteFiles {

	private static final Logger LOGGER = LogManager.getLogger(ReplayOrDeleteFiles.class);

	public ReplayOrDeleteFiles(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
	}
	
	private Sandbox sandbox;
	
	
	public void replay() throws ArcException
	{
		reinstate();
		
		cleanToDelete();
	}
	
	
	/**
	 * Méthode pour rejouer des fichiers
	 *
	 * @param connexion
	 * @param tablePil
	 * @throws ArcException
	 */
	public void reinstate() throws ArcException {
		LoggerHelper.info(LOGGER, "reinstateWithRename");

		Connection connection=sandbox.getConnection();
		String envExecution=sandbox.getSchema();
		
		// on cherche tous les containers contenant un fichier à rejouer
		// on remet l'archive à la racine
		
		List<String> containerList = ReplayOrDeleteFilesDao.execQuerySelectArchiveToReplay(connection, envExecution);
		
		if (!containerList.isEmpty()) {
			PropertiesHandler properties = PropertiesHandler.getInstance();
			String repertoire = properties.getBatchParametersDirectory();
			String envDir = envExecution.replace(".", "_").toUpperCase();

			for (String s : containerList) {

				String entrepot = ManipString.substringBeforeFirst(s, "_");
				String archive = ManipString.substringAfterFirst(s, "_");

				String dirIn = ApiReceptionService.directoryReceptionEntrepotArchive(repertoire, envDir, entrepot);
				String dirOut = ApiReceptionService.directoryReceptionEntrepot(repertoire, envDir, entrepot);

				ApiReceptionService.deplacerFichier(dirIn, dirOut, archive, archive);

			}

		}

		ReplayOrDeleteFilesDao.execQueryDeleteArchiveToReplay(connection, envExecution);
	}

	/**
	 * Suppression dans la table de pilotage des fichiers qui ont été marqué par la
	 * MOA (via la colonne to_delete de la table de pilotage);
	 *
	 * @param connexion
	 * @param tablePil
	 * @throws ArcException
	 */
	private void cleanToDelete() throws ArcException {
		LoggerHelper.info(LOGGER, "Delete file marked by user as to be deleted");
		
		Connection connection=sandbox.getConnection();
		String envExecution=sandbox.getSchema();
		
		ReplayOrDeleteFilesDao.execQueryDeleteFileToDelete(connection, envExecution);
	}


	
	
}
