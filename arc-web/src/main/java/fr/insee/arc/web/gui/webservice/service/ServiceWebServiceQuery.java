package fr.insee.arc.web.gui.webservice.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceWebServiceQuery extends InteractorWebservice {


    public String selectWebserviceQuery(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    public String addWebserviceQuery(Model model) {
        this.vObjectService.insert(views.getViewWebserviceQuery());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String updateWebserviceQuery(Model model) {
    	this.vObjectService.update(views.getViewWebserviceQuery());
    	return generateDisplay(model, RESULT_SUCCESS);
    }

    public String sortWebserviceQuery(Model model) {
        this.vObjectService.sort(views.getViewWebserviceQuery());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String deleteWebserviceQuery(Model model) {
        this.vObjectService.delete(views.getViewWebserviceQuery());
        return generateDisplay(model, RESULT_SUCCESS);
    }
	
}
