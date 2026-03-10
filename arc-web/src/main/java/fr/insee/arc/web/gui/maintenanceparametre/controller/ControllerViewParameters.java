package fr.insee.arc.web.gui.maintenanceparametre.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.maintenanceparametre.service.ServiceViewParameters;

@Controller
public class ControllerViewParameters extends ServiceViewParameters {
	
    @RequestMapping("/secure/selectParameters")
    public String selectParametersAction(Model model) {
    	return selectParameters(model);
    }

    @RequestMapping("/secure/addParameters")
    public String addParametersAction(Model model) {
    	return addParameters(model);
    }

    @RequestMapping("/secure/deleteParameters")
    public String deleteParametersAction(Model model) {
    	return deleteParameters(model);
    }

    @RequestMapping("/secure/updateParameters")
    public String updateParametersAction(Model model) {
    	return updateParameters(model);
    }

    @RequestMapping("/secure/sortParameters")
    public String sortParametersAction(Model model) {
    	return sortParameters(model);
    }

    @RequestMapping("/secure/startParameters")
    public String startParametersAction(Model model) {
    	return startParameters(model);
    }
	

}
