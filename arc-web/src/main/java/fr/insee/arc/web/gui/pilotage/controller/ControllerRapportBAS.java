package fr.insee.arc.web.gui.pilotage.controller;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.pilotage.service.ServiceViewRapportBAS;

@Service
public class ControllerRapportBAS extends ServiceViewRapportBAS {
 
	@RequestMapping("/selectRapportBAS")
	public String selectRapportBASAction(Model model) {
		return selectRapportBAS(model);
	}

	@RequestMapping("/sortRapportBAS")
	public String sortRapportBASAction(Model model) {
		return sortRapportBAS(model);
	}
	
}