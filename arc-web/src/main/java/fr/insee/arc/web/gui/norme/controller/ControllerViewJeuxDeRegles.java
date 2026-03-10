package fr.insee.arc.web.gui.norme.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.norme.service.ServiceViewJeuxDeRegles;

@Controller
public class ControllerViewJeuxDeRegles extends ServiceViewJeuxDeRegles {

	/**
	 * Action trigger by selecting a rule set in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/selectJeuxDeRegles")
	public String selectRuleSetAction(Model model) {
		return selectRuleSet(model);
	}

	/**
	 * Action trigger by adding a rule set in the GUI. Cannot add a rule set in
	 * production state. Update the GUI and the database
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/addJeuxDeRegles")
	public String addRuleSetAction(Model model) {
		return addRuleSet(model);
	}

	/**
	 * Action trigger by deleting a rule set in the GUI. Cannot delete a rule state
	 * in the PRODUCTIONS state. Update the GUI and the database
	 * 
	 * @return sucess
	 */
	@RequestMapping("/secure/deleteJeuxDeRegles")
	public String deleteRuleSetAction(Model model) {
		return deleteRuleSet(model);
	}

	/**
	 * Action trigger by updating a rule set in the GUI. Update the GUI and the
	 * database.
	 * 
	 * If the rule set is send to production, program an initialization for the current day
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/updateJeuxDeRegles")
	public String updateRuleSetAction(Model model) {
		return updateRuleSet(model);
	}

	/**
	 * Action trigger by sorting the ruleset in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/sortJeuxDeRegles")
	public String sortRuleSetAction(Model model) {
		return sortRuleSet(model);
	}

	/**
	 * Get all the rules from on rule set, export them in CSV, zip and send to user
	 * 
	 * @return
	 */
	@RequestMapping("/secure/downloadJeuxDeRegles")
	public String downloadJeuxDeReglesAction(Model model, HttpServletResponse response) {
		return downloadJeuxDeRegles(model, response);
	}

}
