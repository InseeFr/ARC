package fr.insee.arc.web.gui.norme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.norme.service.ServiceViewModule;

@Controller
public class ControllerViewModule extends ServiceViewModule {

	/**
	 * Action trigger by selecting a module in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/selectModules")
	public String selectModulesAction(Model model) {
		return selectModules(model);
	}
	
}
