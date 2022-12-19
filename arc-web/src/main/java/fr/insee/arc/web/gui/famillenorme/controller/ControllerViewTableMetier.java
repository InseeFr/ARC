package fr.insee.arc.web.gui.famillenorme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.famillenorme.service.ServiceViewTableMetier;

@Controller
public class ControllerViewTableMetier extends ServiceViewTableMetier {

	@RequestMapping("/selectTableMetier")
	public String selectTableMetierAction(Model model) {
		return selectTableMetier(model);
	}
	
	@RequestMapping("/addTableMetier")
	public String addTableMetierAction(Model model) {
		return addTableMetier(model);
	}
	
	@RequestMapping("/sortTableMetier")
	public String sortTableMetierAction(Model model) {
		return sortTableMetier(model);
	}
		
	@RequestMapping("/deleteTableMetier")
	public String deleteTableMetierAction(Model model) {
		return deleteTableMetier(model);
	}

}
