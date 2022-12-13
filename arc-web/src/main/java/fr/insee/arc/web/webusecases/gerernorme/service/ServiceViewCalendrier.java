package fr.insee.arc.web.webusecases.gerernorme.service;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class ServiceViewCalendrier extends HubServiceGererNorme {

	/**
	 * Action trigger by selecting a calendar in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	public String selectCalendrier(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a calendar in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return success
	 */
	public String addCalendrier(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.viewCalendrier);
	}

	/**
	 * Action trigger by deleting a calendar in the GUI. Cannot delete a activated
	 * calendar. Update the GUI and the database
	 * 
	 * @return
	 */
	public String deleteCalendrier(Model model) {
		// get the selected calendar
		Map<String, ArrayList<String>> selection = viewCalendrier.mapContentSelected();
		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			// Check actived calendar (code 1)
			if ("1".equals(etat)) {
				this.viewCalendrier.setMessage("Caution, cannot delete a active calendar");
			} else {
				this.vObjectService.delete(viewCalendrier);
			}
		} else {
			this.viewJeuxDeRegles.setMessage("You didn't select anything");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by updating a calendar in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return success
	 */
	public String updateCalendrier(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.viewCalendrier);
	}

	public String sortCalendrier(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewCalendrier);
	}

}
