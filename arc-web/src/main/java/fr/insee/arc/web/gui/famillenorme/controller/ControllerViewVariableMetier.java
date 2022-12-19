package fr.insee.arc.web.gui.famillenorme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.famillenorme.service.ServiceViewVariableMetier;

@Controller
public class ControllerViewVariableMetier extends ServiceViewVariableMetier {


	@RequestMapping("/selectVariableMetier")
	public String selectVariableMetierAction(Model model) {
		return selectVariableMetier(model);
	}

	@RequestMapping("/addVariableMetier")
	public String addVariableMetierAction(Model model) {
		return addVariableMetier(model);
	}

	@RequestMapping("/sortVariableMetier")
	public String sortVariableMetierAction(Model model) {
		return sortVariableMetier(model);
	}

	@RequestMapping("/deleteVariableMetier")
	public String deleteVariableMetierAction(Model model) {
		return deleteVariableMetier(model);
	}

	@RequestMapping("/updateVariableMetier")
	public String updateVariableMetierAction(Model model) {
		return updateVariableMetier(model);
	}
}
