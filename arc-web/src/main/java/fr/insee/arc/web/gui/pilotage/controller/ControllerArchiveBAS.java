package fr.insee.arc.web.gui.pilotage.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.pilotage.service.ServiceViewArchiveBAS;

@Controller
public class ControllerArchiveBAS extends ServiceViewArchiveBAS {

	/**
	 * Téléchargement d'enveloppe contenu dans le dossier d'archive
	 *
	 * @return
	 */
	@RequestMapping("/secure/downloadEnveloppeFromArchiveBAS")
	public void downloadEnveloppeFromArchiveBASAction(HttpServletResponse response) {
		downloadEnveloppeFromArchiveBAS(response);
	}
	
}