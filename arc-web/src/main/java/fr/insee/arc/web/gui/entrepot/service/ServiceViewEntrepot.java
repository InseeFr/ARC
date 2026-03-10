package fr.insee.arc.web.gui.entrepot.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewEntrepot extends InteractorEntrepot {

	public String selectEntrepot(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
    }

    public String addEntrepot(Model model) {
        this.vObjectService.insert(views.getViewEntrepot());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String deleteEntrepot(Model model) {
        this.vObjectService.delete(views.getViewEntrepot());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String updateEntrepot(Model model) {
        this.vObjectService.update(views.getViewEntrepot());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String sortEntrepot(Model model) {
        this.vObjectService.sort(views.getViewEntrepot());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String startEntrepot(Model model) {
        return generateDisplay(model, RESULT_SUCCESS);
    }
    
}
