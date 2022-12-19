package fr.insee.arc.web.gui.pilotage.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.pilotage.service.ServiceArchiveBAS;

@Controller
public class ControllerArchiveBAS extends ServiceArchiveBAS {

	/**
	 * Téléchargement d'enveloppe contenu dans le dossier d'archive
	 *
	 * @return
	 */
	@RequestMapping("/downloadEnveloppeFromArchiveBAS")
	public void downloadEnveloppeFromArchiveBASAction(HttpServletResponse response) {
		downloadEnveloppeFromArchiveBAS(response);
	}
	
}