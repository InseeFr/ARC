package fr.insee.arc.web.gui.famillenorme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.famillenorme.service.ServiceViewHostAllowed;

@Controller
public class ControllerViewHostAllowed extends ServiceViewHostAllowed {

	@RequestMapping("/secure/selectHostAllowed")
	public String selectHostAllowedAction(Model model) {
		return selectHostAllowed(model);
	}

	@RequestMapping("/secure/addHostAllowed")
	public String addHostAllowedAction(Model model) {
		return addHostAllowed(model);
	}

	@RequestMapping("/secure/deleteHostAllowed")
	public String deleteHostAllowedAction(Model model) {
		return deleteHostAllowed(model);
	}

	@RequestMapping("/secure/updateHostAllowed")
	public String updateHostAllowedAction(Model model) {
		return updateHostAllowed(model);
	}

	@RequestMapping("/secure/sortHostAllowed")
	public String sortHostAllowedAction(Model model) {
		return sortHostAllowed(model);
	}
	
}
