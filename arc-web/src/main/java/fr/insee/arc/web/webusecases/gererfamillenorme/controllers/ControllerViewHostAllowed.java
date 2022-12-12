package fr.insee.arc.web.webusecases.gererfamillenorme.controllers;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.webusecases.gererfamillenorme.services.ServiceViewHostAllowed;

public class ControllerViewHostAllowed extends ServiceViewHostAllowed {

	@RequestMapping("/selectHostAllowed")
	public String selectHostAllowedAction(Model model) {
		return selectHostAllowed(model);
	}

	@RequestMapping("/addHostAllowed")
	public String addHostAllowedAction(Model model) {
		return addHostAllowed(model);
	}

	@RequestMapping("/deleteHostAllowed")
	public String deleteHostAllowedAction(Model model) {
		return deleteHostAllowed(model);
	}

	@RequestMapping("/updateHostAllowed")
	public String updateHostAllowedAction(Model model) {
		return updateHostAllowed(model);
	}

	@RequestMapping("/sortHostAllowed")
	public String sortHostAllowedAction(Model model) {
		return sortHostAllowed(model);
	}
	
}
