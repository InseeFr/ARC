package fr.insee.arc.web.gui.query.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewQuery extends InteractorQuery {


	public String selectQuery(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String sortQuery(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewQuery());
	}

	
}
