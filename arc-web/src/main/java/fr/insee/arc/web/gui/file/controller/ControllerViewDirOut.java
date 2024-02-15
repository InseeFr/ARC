package fr.insee.arc.web.gui.file.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.file.service.ServiceViewDirOut;

@Controller
public class ControllerViewDirOut extends ServiceViewDirOut {
	
	@RequestMapping({"/debug/selectDirOut", "/debug/seeDirOut"})
	public String seeDirOutAction (Model model) {
		return seeDirOut(model);
	}

	@RequestMapping("/debug/sortDirOut")
	public String sortDirOutAction (Model model) {
		return sortDirOut(model);
	}

	@RequestMapping("/debug/transferDirOut")
	public String transferDirOutAction(Model model) {
		return transferDirOut(model);
	}


	@RequestMapping("/debug/copyDirOut")
	public String copyDirOutAction(Model model) {
		return copyDirOut(model);
	}

	@RequestMapping("/debug/updateDirOut")
	public String updateDirOutAction(Model model) {
		return updateDirOut(model);
	}


	@RequestMapping("/debug/addDirOut")
	public String addDirOutAction(Model model) {
		return addDirOut(model);
	}

	@RequestMapping("/debug/deleteDirOut")
	public String delDirOutAction(Model model) {
		return delDirOut(model);
	}
	
	@RequestMapping("/debug/downloadDirOut")
	public String downloadDirOutAction(Model model, HttpServletResponse response) {
		return downloadDirOut(model, response);
	}
	
}
