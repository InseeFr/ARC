package fr.insee.arc.web.gui.norme.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewCalendrier extends InteractorNorme {

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
		return addLineVobject(model, RESULT_SUCCESS, this.views.getViewCalendrier());
	}

	/**
	 * Action trigger by deleting a calendar in the GUI. Cannot delete a activated
	 * calendar. Update the GUI and the database
	 * 
	 * @return
	 */
	public String deleteCalendrier(Model model) {
		// get the selected calendar
		Map<String, List<String>> selection = views.getViewCalendrier().mapContentSelected();
		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			// Check actived calendar (code 1)
			if ("1".equals(etat)) {
				this.views.getViewCalendrier().setMessage("normManagement.calendar.warning");
			} else {
				this.vObjectService.delete(views.getViewCalendrier());
			}
		} else {
			this.views.getViewJeuxDeRegles().setMessage("general.noSelection");
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
		return updateVobject(model, RESULT_SUCCESS, this.views.getViewCalendrier());
	}

	public String sortCalendrier(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.views.getViewCalendrier());
	}

}
