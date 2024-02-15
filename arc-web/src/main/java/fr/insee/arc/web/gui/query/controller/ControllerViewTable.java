package fr.insee.arc.web.gui.query.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.query.service.ServiceViewTable;

@Controller
public class ControllerViewTable extends ServiceViewTable {
	

	@RequestMapping({"/debug/selectTable", "/debug/seeTable"})
	public String seeTableAction(Model model) {
		return seeTable(model);
	}

	@RequestMapping("/debug/sortTable")
	public String sortTableAction(Model model) {
		return sortTable(model);
	}

}
