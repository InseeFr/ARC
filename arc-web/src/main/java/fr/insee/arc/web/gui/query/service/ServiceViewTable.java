package fr.insee.arc.web.gui.query.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewTable extends InteractorQuery {
	
	public String seeTable(Model model) {
		HashMap<String, ArrayList<String>> mapContentSelected = views.getViewTable().mapContentSelected();
		if (!mapContentSelected.isEmpty()) {
			this.myQuery = "select * from " + this.mySchema+"." + mapContentSelected.get("tablename").get(0) + " limit 10 ";
			model.addAttribute("myQuery", myQuery);
		}
		return basicAction(model, RESULT_SUCCESS);
	}

	public String sortTable(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewTable());
	}

}
