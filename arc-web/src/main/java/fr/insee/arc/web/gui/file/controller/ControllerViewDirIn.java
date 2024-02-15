package fr.insee.arc.web.gui.file.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.file.service.ServiceViewDirIn;

@Controller
public class ControllerViewDirIn extends ServiceViewDirIn {

	@RequestMapping({"/debug/selectDirIn", "/debug/seeDirIn"})
	public String seeDirInAction (Model model) {
		return seeDirIn(model);
	}

	@RequestMapping("/debug/sortDirIn")
	public String sortDirInAction (Model model) {
		return sortDirIn(model);
	}

	@RequestMapping("/debug/transferDirIn")
	public String transferDirInAction(Model model) {
		return transferDirIn(model);
	}


	@RequestMapping("/debug/copyDirIn")
	public String copyDirInAction(Model model) {
		return copyDirIn(model);
	}

	@RequestMapping("/debug/updateDirIn")
	public String updateDirInAction(Model model) {
		return updateDirIn(model);
	}


	@RequestMapping("/debug/addDirIn")
	public String addDirInAction(Model model) {
		return addDirIn(model);
	}

	@RequestMapping("/debug/deleteDirIn")
	public String delDirInAction(Model model) {
		return delDirIn(model);
	}
	
	@RequestMapping("/debug/downloadDirIn")
	public String downloadDirInAction(Model model, HttpServletResponse response) {
		return downloadDirIn(model, response);
	}
	
}
