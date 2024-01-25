package fr.insee.arc.web.gui.pilotage.service;

import java.nio.file.Paths;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.insee.arc.utils.exception.ArcException;

@Service
public class ServiceViewArchiveBAS extends InteractorPilotage {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewArchiveBAS.class);

	/**
	 * Téléchargement d'enveloppe contenu dans le dossier d'archive
	 *
	 * @return
	 */
	public void downloadEnveloppeFromArchiveBAS(HttpServletResponse response) {

		loggerDispatcher.trace("*** Téléchargement des enveloppes à partir de l'archive ***", LOGGER);
		
		// récupération de la liste des noms d'enveloppe
		List<String> selection = views.getViewArchiveBAS().mapContentSelected().get("nom_archive");

		initializeArchiveBAS(views.getViewArchiveBAS(), views.getViewEntrepotBAS());

		String sandboxRootDirectory = Paths.get(this.repertoire, getBacASable().toUpperCase()).toString();
		
		try {
			this.dao.execQueryDownloadArchive(response, views.getViewArchiveBAS(), selection, sandboxRootDirectory);
		} catch (ArcException e) {
			this.views.getViewArchiveBAS().setMessage("familyManagement.table.error.invalidname");
		}

	}

}