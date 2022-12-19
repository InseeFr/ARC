package fr.insee.arc.web.gui.pilotage.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceEntrepotBAS extends InteractorPilotage {

	/**
	 * Fabrication d'une table temporaire avec comme contenu le nom des archives
	 * d'un entrepot donné puis Ouverture d'un VObject sur cette table
	 *
	 * @return
	 */
	public String visualiserEntrepotBAS(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

}