package fr.insee.arc.web.gui.nomenclature.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewSchemaNmcl extends InteractorNomenclature {

	public String selectSchemaNmcl(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}
	
	public String sortSchemaNmcl(Model model) {
		this.vObjectService.sort(views.getViewSchemaNmcl());
		return basicAction(model, RESULT_SUCCESS);
	}
	
}
