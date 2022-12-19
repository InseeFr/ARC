package fr.insee.arc.web.gui.nomenclature.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewNomenclature extends InteractorNomenclature {


    public String selectNomenclature(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    public String sortNomenclature(Model model) {
        this.vObjectService.sort(views.getViewNomenclature());
        return basicAction(model, RESULT_SUCCESS);
    }
    
    
	
}
