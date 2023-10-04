package fr.insee.arc.web.gui.famillenorme.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.utils.exception.ArcException;

@Service
public class ServiceViewVariableMetier extends InteractorFamilleNorme {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewVariableMetier.class);


	public String selectVariableMetier(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addVariableMetier(Model model) {
		try {
			String idFamilleSelected = views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0);
			// if family is selected
			if (idFamilleSelected != null) {	
				dao.execQueryAddVariableMetier(this.views.getViewVariableMetier(), idFamilleSelected);	
			}
		} catch (ArcException e) {
			this.views.getViewVariableMetier().setMessage(e.getMessage());
			e.logFullException();
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortVariableMetier(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewVariableMetier());
	}

	public String deleteVariableMetier(Model model) {
		
		try {
			String idFamilleSelected = views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0);
			// if family is selected
			if (idFamilleSelected != null) {	
				dao.execQueryDeleteVariableMetier(this.views.getViewVariableMetier(), idFamilleSelected);	
			}
		} catch (ArcException e) {
			this.views.getViewVariableMetier().setMessage(e.getMessage());
			e.logFullException();
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String updateVariableMetier(Model model) {
		try {
			String idFamilleSelected = views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0);
			// if family is selected
			if (idFamilleSelected != null) {
				dao.execQueryUpdateVariableMetier(this.views.getViewVariableMetier(), idFamilleSelected);
			}
		} catch (ArcException e) {
			this.views.getViewVariableMetier().setMessage(e.getMessage());
			e.logFullException();
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}
	
}
