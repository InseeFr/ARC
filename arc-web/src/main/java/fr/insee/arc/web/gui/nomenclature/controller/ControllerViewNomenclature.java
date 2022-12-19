package fr.insee.arc.web.gui.nomenclature.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.nomenclature.service.ServiceViewNomenclature;

@Controller
public class ControllerViewNomenclature extends ServiceViewNomenclature {


    @RequestMapping("/selectNomenclature")
    public String selectNomenclatureAction(Model model) {
        return selectNomenclature(model);
    }

    @RequestMapping("/sortNomenclature")
    public String sortNomenclatureAction(Model model) {
        return sortNomenclature(model);
    }
    
    
	
}
