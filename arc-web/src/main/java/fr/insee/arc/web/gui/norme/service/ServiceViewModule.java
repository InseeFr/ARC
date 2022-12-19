package fr.insee.arc.web.gui.norme.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewModule extends InteractorNorme {

	/**
	 * Action trigger by selecting a module in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	public String selectModules(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

}
