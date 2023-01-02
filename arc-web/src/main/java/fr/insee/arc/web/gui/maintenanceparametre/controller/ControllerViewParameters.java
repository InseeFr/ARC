package fr.insee.arc.web.gui.maintenanceparametre.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.maintenanceparametre.service.ServiceViewParameters;

@Controller
public class ControllerViewParameters extends ServiceViewParameters {
	
    @RequestMapping("/selectParameters")
    public String selectParametersAction(Model model) {
    	return selectParameters(model);
    }

    @RequestMapping("/addParameters")
    public String addParametersAction(Model model) {
    	return addParameters(model);
    }

    @RequestMapping("/deleteParameters")
    public String deleteParametersAction(Model model) {
    	return deleteParameters(model);
    }

    @RequestMapping("/updateParameters")
    public String updateParametersAction(Model model) {
    	return updateParameters(model);
    }

    @RequestMapping("/sortParameters")
    public String sortParametersAction(Model model) {
    	return sortParameters(model);
    }

    @RequestMapping("/startParameters")
    public String startParametersAction(Model model) {
    	return startParameters(model);
    }
	

}
