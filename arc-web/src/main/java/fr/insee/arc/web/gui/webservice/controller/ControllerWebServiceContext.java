package fr.insee.arc.web.gui.webservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.webservice.service.ServiceWebServiceContext;

@Controller
public class ControllerWebServiceContext extends ServiceWebServiceContext {


    @RequestMapping("/secure/selectWebserviceContext")
    public String selectWebserviceContextAction(Model model) {
        return selectWebserviceContext(model);
    }

    @RequestMapping("/secure/addWebserviceContext")
    public String addWebserviceContextAction(Model model) {
        return addWebserviceContext(model);
    }

    @RequestMapping("/secure/updateWebserviceContext")
    public String updateWebserviceContextAction(Model model) {
    	return updateWebserviceContext(model);
    }

    @RequestMapping("/secure/sortWebserviceContext")
    public String sortWebserviceContextAction(Model model) {
        this.vObjectService.sort(views.getViewWebserviceContext());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/secure/deleteWebserviceContext")
    public String deleteWebserviceContextAction(Model model) {
        return deleteWebserviceContext(model);
    }
	
}
