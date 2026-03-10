package fr.insee.arc.web.gui.file.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewFile extends InteractorFile {

	public String selectFile(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
	}
	
}
