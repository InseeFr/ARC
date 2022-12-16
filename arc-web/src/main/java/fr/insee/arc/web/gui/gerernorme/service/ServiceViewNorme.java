package fr.insee.arc.web.gui.gerernorme.service;

import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewNorme extends HubServiceGererNorme {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewNorme.class);

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
		Map<String, ArrayList<String>> selection = views.getViewNorme().mapContentSelected();

		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			loggerDispatcher.info("Norm state : " + etat, LOGGER);
			// Check actived norm (code 1)
			if ("1".equals(etat)) {
				views.getViewNorme().setMessage("Caution, cannot delete a activated norm");
			} else {
				this.vObjectService.delete(views.getViewNorme());
			}
		} else {
			views.getViewNorme().setMessage("You didn't select anything");
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
