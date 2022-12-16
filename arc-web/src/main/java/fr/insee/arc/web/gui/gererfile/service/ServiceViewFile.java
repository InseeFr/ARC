package fr.insee.arc.web.gui.gererfile.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewFile extends HubServiceGererFileAction {

	public String selectFile(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
	}
	
}
