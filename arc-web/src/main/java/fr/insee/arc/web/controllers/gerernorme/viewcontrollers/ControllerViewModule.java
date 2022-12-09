package fr.insee.arc.web.controllers.gerernorme.viewcontrollers;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.controllers.gerernorme.ControllerGererNorme;

@Component
public class ControllerViewModule extends ControllerGererNorme {

	/**
	 * Action trigger by selecting a module in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectModules")
	public String selectModulesAction(Model model) {
		return selectModules(model);
	}
	
}
