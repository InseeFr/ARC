package fr.insee.arc.web.gui.norme.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewNorme extends InteractorNorme {

	/**
	 * Action trigger by selecting a norm in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	public String selectNorme(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a norm in the GUI.
	 * 
	 * @return success
	 */
	public String addNorme(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, views.getViewNorme());
	}

	/**
	 * Action trigger by deleting a norm in the GUI. Cannot delete a active norm
	 *
	 * @return success
	 */
	public String deleteNorme(Model model) {

		// Get the gui selection
		Map<String, List<String>> selection = views.getViewNorme().mapContentSelected();

		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			// Check actived norm (code 1)
			if ("1".equals(etat)) {
				views.getViewNorme().setMessage("normManagement.delete.warning");
			} else {
				this.vObjectService.delete(views.getViewNorme());
			}
		} else {
			views.getViewNorme().setMessage("general.noSelection");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by updating a norm in the GUI. Update the GUI
	 */
	public String updateNorme(Model model) {
		return updateVobject(model, RESULT_SUCCESS, views.getViewNorme());
	}

	/**
	 * Action trigger by sorting a norm in the GUI. Update the GUI
	 */
	public String sortNorme(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewNorme());
	}

}
