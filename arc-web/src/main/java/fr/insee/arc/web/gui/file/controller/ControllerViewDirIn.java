package fr.insee.arc.web.gui.file.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.file.service.ServiceViewDirIn;

@Controller
public class ControllerViewDirIn extends ServiceViewDirIn {

	@RequestMapping({"/secure/selectDirIn", "/secure/seeDirIn"})
	public String seeDirInAction (Model model) {
		return seeDirIn(model);
	}

	@RequestMapping("/secure/sortDirIn")
	public String sortDirInAction (Model model) {
		return sortDirIn(model);
	}

	@RequestMapping("/secure/transferDirIn")
	public String transferDirInAction(Model model) {
		return transferDirIn(model);
	}


	@RequestMapping("/secure/copyDirIn")
	public String copyDirInAction(Model model) {
		return copyDirIn(model);
	}

	@RequestMapping("/secure/updateDirIn")
	public String updateDirInAction(Model model) {
		return updateDirIn(model);
	}


	@RequestMapping("/secure/addDirIn")
	public String addDirInAction(Model model) {
		return addDirIn(model);
	}

	@RequestMapping("/secure/deleteDirIn")
	public String delDirInAction(Model model) {
		return delDirIn(model);
	}
	
	@RequestMapping("/secure/downloadDirIn")
	public String downloadDirInAction(Model model, HttpServletResponse response) {
		return downloadDirIn(model, response);
	}
	
}
