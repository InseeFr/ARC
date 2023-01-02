package fr.insee.arc.web.gui.maintenanceparametre.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewParameters extends InteractorMaintenanceParameters {

    public String selectParameters(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
    }

    public String addParameters(Model model) {
        this.vObjectService.insert(views.getViewParameters());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String deleteParameters(Model model) {
        this.vObjectService.delete(views.getViewParameters());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String updateParameters(Model model) {
        this.vObjectService.update(views.getViewParameters());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String sortParameters(Model model) {
        this.vObjectService.sort(views.getViewParameters());
        return generateDisplay(model, RESULT_SUCCESS);
    }

}
