package fr.insee.arc.web.gui.gererfile.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.gererfile.service.ServiceViewDirOut;

@Controller
public class ControllerViewDirOut extends ServiceViewDirOut {
	
	@RequestMapping({"/selectDirOut", "/seeDirOut"})
	public String seeDirOutAction (Model model) {
		return seeDirOut(model);
	}

	@RequestMapping("/sortDirOut")
	public String sortDirOutAction (Model model) {
		return sortDirOut(model);
	}

	@RequestMapping("/transferDirOut")
	public String transferDirOutAction(Model model) {
		return transferDirOut(model);
	}


	@RequestMapping("/copyDirOut")
	public String copyDirOutAction(Model model) {
		return copyDirOut(model);
	}

	@RequestMapping("/updateDirOut")
	public String updateDirOutAction(Model model) {
		return updateDirOut(model);
	}


	@RequestMapping("/addDirOut")
	public String addDirOutAction(Model model) {
		return addDirOut(model);
	}

	@RequestMapping("/deleteDirOut")
	public String delDirOutAction(Model model) {
		return delDirOut(model);
	}
	
	@RequestMapping("/downloadDirOut")
	public String downloadDirOutAction(Model model, HttpServletResponse response) {
		return downloadDirOut(model, response);
	}
	
}
