package fr.insee.arc.web.gui.query.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewQuery extends InteractorQuery {


	public String selectQuery(Model model) {
		model.addAttribute("myDbConnection", myDbConnection);
		return basicAction(model, RESULT_SUCCESS);
	}

	public String sortQuery(Model model) {
		model.addAttribute("myDbConnection", myDbConnection);
		return sortVobject(model, RESULT_SUCCESS, views.getViewQuery());
	}

	
}
