package fr.insee.arc.web.gui.norme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.norme.service.ServiceViewJeuxDeReglesCopie;

@Controller
public class ControllerViewJeuxDeReglesCopie extends ServiceViewJeuxDeReglesCopie {

	/**
	 * Action trigger by requesting the load rules of the register rule set to copy
	 * in the actual rule set
	 * 
	 * @return
	 */
	@RequestMapping("/secure/selectJeuxDeReglesChargementCopie")
	public String selectJeuxDeReglesChargementCopieAction(Model model) {
		return selectJeuxDeReglesChargementCopie(model);
	}

	/**
	 * Action trigger by requesting the structurize rules of the register rule set
	 * to copy in the actual rule set
	 * 
	 * @return
	 */
	@RequestMapping("/secure/selectJeuxDeReglesNormageCopie")
	public String selectJeuxDeReglesNormageCopieAction(Model model) {
		return selectJeuxDeReglesNormageCopie(model);
	}

	/**
	 * Action trigger by requesting the control rules of the register rule set to
	 * copy in the actual rule set
	 * 
	 * @return
	 */
	@RequestMapping("/secure/selectJeuxDeReglesControleCopie")
	public String selectJeuxDeReglesControleCopieAction(Model model) {
		return selectJeuxDeReglesControleCopie(model);
	}

	/**
	 * Action to copy mapping rules
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/secure/selectJeuxDeReglesMappingCopie")
	public String selectJeuxDeReglesMappingCopieAction(Model model) {
		return selectJeuxDeReglesMappingCopie(model);
	}

	/**
	 * Action to copy expression rules
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/secure/selectJeuxDeReglesExpressionCopie")
	public String selectJeuxDeReglesExpressionCopieAction(Model model) {
		return selectJeuxDeReglesExpressionCopie(model);
	}

	@RequestMapping("/secure/selectJeuxDeReglesCopie")
	public String selectJeuxDeReglesCopieAction(Model model) {
		return selectJeuxDeReglesCopie(model);
	}

	@RequestMapping("/secure/copieJeuxDeRegles")
	public String copieJeuxDeReglesAction(Model model) {
		return copieJeuxDeRegles(model);
	}

}
