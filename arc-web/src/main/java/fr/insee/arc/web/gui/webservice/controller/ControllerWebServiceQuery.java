package fr.insee.arc.web.gui.webservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.webservice.service.ServiceWebServiceQuery;

@Controller
public class ControllerWebServiceQuery extends ServiceWebServiceQuery  {


    @RequestMapping("/selectWebserviceQuery")
    public String selectWebserviceQueryAction(Model model) {
        return selectWebserviceQuery(model);
    }

    @RequestMapping("/addWebserviceQuery")
    public String addWebserviceQueryAction(Model model) {
        return addWebserviceQuery(model);
    }

    @RequestMapping("/updateWebserviceQuery")
    public String updateWebserviceQueryAction(Model model) {
    	return updateWebserviceQuery(model);
    }

    @RequestMapping("/sortWebserviceQuery")
    public String sortWebserviceQueryAction(Model model) {
        return sortWebserviceQuery(model);
    }

    @RequestMapping("/deleteWebserviceQuery")
    public String deleteWebserviceQueryAction(Model model) {
        return deleteWebserviceQuery(model);
    }
	
}
