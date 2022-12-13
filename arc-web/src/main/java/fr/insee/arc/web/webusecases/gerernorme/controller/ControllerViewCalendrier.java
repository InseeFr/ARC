package fr.insee.arc.web.webusecases.gerernorme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.webusecases.gerernorme.service.ServiceViewCalendrier;

@Controller
public class ControllerViewCalendrier extends ServiceViewCalendrier {

	/**
	 * Action trigger by selecting a calendar in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectCalendrier")
	public String selectCalendrierAction(Model model) {
		return selectCalendrier(model);
	}

	/**
	 * Action trigger by adding a calendar in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return success
	 */
	@RequestMapping("/addCalendrier")
	public String addCalendrierAction(Model model) {
		return addCalendrier(model);
	}

	/**
	 * Action trigger by deleting a calendar in the GUI. Cannot delete a activated
	 * calendar. Update the GUI and the database
	 * 
	 * @return
	 */
	@RequestMapping("/deleteCalendrier")
	public String deleteCalendrierAction(Model model) {
		return deleteCalendrier(model);
	}

	/**
	 * Action trigger by updating a calendar in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return success
	 */
	@RequestMapping("/updateCalendrier")
	public String updateCalendrierAction(Model model) {
		return updateCalendrier(model);
	}

	@RequestMapping("/sortCalendrier")
	public String sortCalendrierAction(Model model) {
		return sortCalendrier(model);
	}

}
