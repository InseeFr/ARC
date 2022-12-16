package fr.insee.arc.web.webusecases.gererfamillenorme.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewClient extends HubServiceGererFamilleNorme {
	public String selectClient(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addClient(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, views.getViewClient());
	}

	public String deleteClient(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, views.getViewClient());
	}

	public String updateClient(Model model) {
		return updateVobject(model, RESULT_SUCCESS, views.getViewClient());
	}

	public String sortClient(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewClient());
	}
}
