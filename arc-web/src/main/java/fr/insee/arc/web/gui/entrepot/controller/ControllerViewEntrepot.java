package fr.insee.arc.web.gui.entrepot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.entrepot.service.ServiceViewEntrepot;

@Controller
public class ControllerViewEntrepot extends ServiceViewEntrepot {
	
	@RequestMapping("/secure/selectEntrepot")
    public String selectEntrepotAction(Model model) {
    	return selectEntrepot(model);
    }

	@RequestMapping("/secure/addEntrepot")
    public String addEntrepotAction(Model model) {
    	return addEntrepot(model);
    }

    @RequestMapping("/secure/deleteEntrepot")
    public String deleteEntrepotAction(Model model) {
    	return deleteEntrepot(model);
    }

    @RequestMapping("/secure/updateEntrepot")
    public String updateEntrepotAction(Model model) {
    	return updateEntrepot(model);
    }

    @RequestMapping("/secure/sortEntrepot")
    public String sortEntrepotAction(Model model) {
    	return sortEntrepot(model);
    }

    @RequestMapping("/secure/startEntrepot")
    public String startEntrepotAction(Model model) {
    	return startEntrepot(model);
    }

}
