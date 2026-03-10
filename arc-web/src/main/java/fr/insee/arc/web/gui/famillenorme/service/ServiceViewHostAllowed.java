package fr.insee.arc.web.gui.famillenorme.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewHostAllowed extends InteractorFamilleNorme {

	public String selectHostAllowed(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addHostAllowed(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, views.getViewHostAllowed());
	}

	public String deleteHostAllowed(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, views.getViewHostAllowed());
	}

	public String updateHostAllowed(Model model) {
		return updateVobject(model, RESULT_SUCCESS, views.getViewHostAllowed());
	}

	public String sortHostAllowed(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewHostAllowed());
	}
	
}
