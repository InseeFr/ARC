package fr.insee.arc.web.webusecases.gererfile.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.webusecases.gererfile.service.ServiceViewDirIn;

@Controller
public class ControllerViewDirIn extends ServiceViewDirIn {

	@RequestMapping({"/selectDirIn", "/seeDirIn"})
	public String seeDirInAction (Model model) {
		return seeDirIn(model);
	}

	@RequestMapping("/sortDirIn")
	public String sortDirInAction (Model model) {
		return sortDirIn(model);
	}

	@RequestMapping("/transferDirIn")
	public String transferDirInAction(Model model) {
		return transferDirIn(model);
	}


	@RequestMapping("/copyDirIn")
	public String copyDirInAction(Model model) {
		return copyDirIn(model);
	}

	@RequestMapping("/updateDirIn")
	public String updateDirInAction(Model model) {
		return updateDirIn(model);
	}


	@RequestMapping("/addDirIn")
	public String addDirInAction(Model model) {
		return addDirIn(model);
	}

	@RequestMapping("/deleteDirIn")
	public String delDirInAction(Model model) {
		return delDirIn(model);
	}
	
	@RequestMapping("/downloadDirIn")
	public String downloadDirInAction(Model model, HttpServletResponse response) {
		return downloadDirIn(model, response);
	}
	
}
