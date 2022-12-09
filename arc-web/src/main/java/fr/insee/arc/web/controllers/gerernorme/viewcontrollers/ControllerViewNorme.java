package fr.insee.arc.web.controllers.gerernorme.viewcontrollers;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.controllers.gerernorme.ControllerGererNorme;

@Component
public class ControllerViewNorme extends ControllerGererNorme {
	/**
	 * Action trigger to display a norm entry in the GUI
	 *
	 * @return success
	 */
	@RequestMapping("/selectNorme")
	public String selectNormeAction(Model model) {
		return selectNorme(model);
	}
	
	/**
	 * Action trigger by adding a norm entry in the GUI
	 *
	 * @return success
	 */
	@RequestMapping("/addNorme")
	public String addNormeAction(Model model) {
		return addNorme(model);
	}
	
	/**
	 * Action trigger by deleting a norm entry in the GUI. Cannot delete a active norm
	 *
	 * @return success
	 */
	@RequestMapping("/deleteNorme")
	public String deleteNormeAction(Model model) {
		return deleteNorme(model);
	}
	
	/**
	 * Action trigger by updating a norm entry in the GUI. Update the GUI
	 */
	@RequestMapping("/updateNorme")
	public String updateNormeAction(Model model) {
		return updateNorme(model);
	}

	/**
	 * Action trigger by sorting a norm view in the GUI. Update the GUI
	 */
	@RequestMapping("/sortNorme")
	public String sortNormeAction(Model model) {
		return sortNorme(model);
	}
	
	
}
