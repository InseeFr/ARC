package fr.insee.arc.web.gui.webservice.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceWebServiceContext extends InteractorWebservice {

    public String selectWebserviceContext(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    public String addWebserviceContext(Model model) {
        this.vObjectService.insert(views.getViewWebserviceContext());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String updateWebserviceContext(Model model) {
    	this.vObjectService.update(views.getViewWebserviceContext());
    	return generateDisplay(model, RESULT_SUCCESS);
    }

    public String sortWebserviceContext(Model model) {
        this.vObjectService.sort(views.getViewWebserviceContext());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String deleteWebserviceContext(Model model) {
        this.vObjectService.delete(views.getViewWebserviceContext());
        return generateDisplay(model, RESULT_SUCCESS);
    }
	
}
