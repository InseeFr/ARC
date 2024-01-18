package fr.insee.arc.web.gui.pilotage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.pilotage.service.ServiceViewRapportBAS;

@Controller
public class ControllerRapportBAS extends ServiceViewRapportBAS {

	@RequestMapping("/secure/selectRapportBAS")
	public String selectRapportBASAction(Model model) {
		return selectRapportBAS(model);
	}

	@RequestMapping("/secure/sortRapportBAS")
	public String sortRapportBASAction(Model model) {
		return sortRapportBAS(model);
	}

}