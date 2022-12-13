package fr.insee.arc.web.webusecases.gererfamillenorme.services;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class ServiceViewClient extends HubServiceGererFamilleNorme {
	public String selectClient(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addClient(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, getViewClient());
	}

	public String deleteClient(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, getViewClient());
	}

	public String updateClient(Model model) {
		return updateVobject(model, RESULT_SUCCESS, getViewClient());
	}

	public String sortClient(Model model) {
		return sortVobject(model, RESULT_SUCCESS, getViewClient());
	}
}
