package fr.insee.arc.web.gui.pilotage.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewRapportBAS extends InteractorPilotage {

	public String selectRapportBAS(Model model) {

		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortRapportBAS(Model model) {
		this.vObjectService.sort(views.getViewRapportBAS());
		return generateDisplay(model, RESULT_SUCCESS);

	}

}