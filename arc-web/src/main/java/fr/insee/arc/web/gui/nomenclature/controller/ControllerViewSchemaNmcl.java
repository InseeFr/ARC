package fr.insee.arc.web.gui.nomenclature.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.nomenclature.service.ServiceViewSchemaNmcl;

@Controller
public class ControllerViewSchemaNmcl extends ServiceViewSchemaNmcl {
	 
    @RequestMapping("/secure/selectSchemaNmcl")
    public String selectSchemaNmclAction(Model model) {
        return selectSchemaNmcl(model);
    }

    @RequestMapping("/secure/addSchemaNmcl")
    public String addSchemaNmclAction(Model model) {
		return addSchemaNmcl(model);
    }

    @RequestMapping("/secure/updateSchemaNmcl")
    public String updateSchemaNmclAction(Model model) {
        return updateSchemaNmcl(model);
    }

    @RequestMapping("/secure/sortSchemaNmcl")
    public String sortSchemaNmclAction(Model model) {
        return sortSchemaNmcl(model);
    }

    @RequestMapping("/secure/deleteSchemaNmcl")
    public String deleteSchemaNmclAction(Model model) {
        return deleteSchemaNmcl(model);
    }

}
