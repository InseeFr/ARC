package fr.insee.arc.web.gui.file.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.file.service.ServiceViewFile;

@Controller
public class ControllerViewFile extends ServiceViewFile {

	@RequestMapping("/selectFile")
	public String selectFileAction(Model model) {
		return selectFile(model);
	}
}
