package fr.insee.arc.web.gui.pilotage.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.pilotage.service.ServicePilotageProd;

@Controller
public class ControllerPilotageProd extends ServicePilotageProd {
	
	/**
	 * Service permettant de visualiser l'état du batch en production
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/informationInitialisationPROD")
    public String informationInitialisationPRODAction(Model model, HttpServletRequest request) {
    	return informationInitialisationPROD(model, request);
    }

	/**
	 * Service pour retarder l'exécution automatique de la phase d'iniitalisation en production
	 * @param model
	 * @return
	 */
    @RequestMapping("/retarderBatchInitialisationPROD")
    public String retarderBatchInitialisationPRODAction(Model model) {
    	return retarderBatchInitialisationPROD(model);
    }
    
    /**
     * Service correspondant au bouton de demande immédiate de phase d'initialisation en production
     * @param model
     * @return
     */
    @RequestMapping("/demanderBatchInitialisationPROD")
    public String demanderBatchInitialisationPRODAction(Model model) {
    	return demanderBatchInitialisationPROD(model);
    }
    
    /**
     * Service correspondant à l'activation du batch de production
     * @param model
     * @return
     */
    @RequestMapping("/toggleOnPROD")
    public String toggleOnPRODAction(Model model) {
    	return toggleOnPROD(model);
    }

    
    /**
     * Service correspondant à la désactivation du batch de production
     * @param model
     * @return
     */
    @RequestMapping("/toggleOffPROD")
    public String toggleOffPRODAction(Model model) {
    	return toggleOffPROD(model);
    }
	
    /**
     * user is able to copy the rules fast to a production environment
     * @param model
     * @return
     */
    @RequestMapping("/applyRulesProd")
    public String applyRulesProdAction(Model model) {
    	return applyRulesProd(model);
    }
    
    
}
