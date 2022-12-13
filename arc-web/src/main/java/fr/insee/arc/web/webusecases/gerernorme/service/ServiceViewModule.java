package fr.insee.arc.web.webusecases.gerernorme.service;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class ServiceViewModule extends HubServiceGererNorme {

	/**
	 * Action trigger by selecting a module in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	public String selectModules(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

}
