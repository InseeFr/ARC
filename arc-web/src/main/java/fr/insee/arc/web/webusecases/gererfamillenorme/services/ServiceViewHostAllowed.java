package fr.insee.arc.web.webusecases.gererfamillenorme.services;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class ServiceViewHostAllowed extends HubServiceGererFamilleNorme {

	public String selectHostAllowed(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addHostAllowed(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, getViewHostAllowed());
	}

	public String deleteHostAllowed(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, getViewHostAllowed());
	}

	public String updateHostAllowed(Model model) {
		return updateVobject(model, RESULT_SUCCESS, getViewHostAllowed());
	}

	public String sortHostAllowed(Model model) {
		return sortVobject(model, RESULT_SUCCESS, getViewHostAllowed());
	}
	
}
