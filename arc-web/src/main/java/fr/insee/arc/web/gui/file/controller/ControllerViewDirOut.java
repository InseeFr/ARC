package fr.insee.arc.web.gui.file.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.file.service.ServiceViewDirOut;

@Controller
public class ControllerViewDirOut extends ServiceViewDirOut {
	
	@RequestMapping({"/secure/selectDirOut", "/secure/seeDirOut"})
	public String seeDirOutAction (Model model) {
		return seeDirOut(model);
	}

	@RequestMapping("/secure/sortDirOut")
	public String sortDirOutAction (Model model) {
		return sortDirOut(model);
	}

	@RequestMapping("/secure/transferDirOut")
	public String transferDirOutAction(Model model) {
		return transferDirOut(model);
	}


	@RequestMapping("/secure/copyDirOut")
	public String copyDirOutAction(Model model) {
		return copyDirOut(model);
	}

	@RequestMapping("/secure/updateDirOut")
	public String updateDirOutAction(Model model) {
		return updateDirOut(model);
	}


	@RequestMapping("/secure/addDirOut")
	public String addDirOutAction(Model model) {
		return addDirOut(model);
	}

	@RequestMapping("/secure/deleteDirOut")
	public String delDirOutAction(Model model) {
		return delDirOut(model);
	}
	
	@RequestMapping("/secure/downloadDirOut")
	public String downloadDirOutAction(Model model, HttpServletResponse response) {
		return downloadDirOut(model, response);
	}
	
}
