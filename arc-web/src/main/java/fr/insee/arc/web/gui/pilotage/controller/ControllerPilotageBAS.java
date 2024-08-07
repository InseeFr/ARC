package fr.insee.arc.web.gui.pilotage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.web.gui.pilotage.service.ServiceViewPilotageBAS;

@Controller
public class ControllerPilotageBAS extends ServiceViewPilotageBAS {
	
	/**
	 * Entering sandbox monitoring from main menu build the database and filesystem
	 * 
	 * @return
	 * @throws ArcException 
	 */
	@RequestMapping("/secure/enterPilotageBAS")
	public String enterPilotageBASAction(Model model) throws ArcException {
		return enterPilotageBAS(model);
	}

	@PostMapping("/secure/selectPilotageBAS")
	public String selectPilotageBASAction(Model model) {
		return selectPilotageBAS(model);
	}
	
	@PostMapping("/secure/updateEnvDescription")
	public String updateEnvDescriptionAction(Model model) {
		return updateEnvDescription(model);
	}
	
	@RequestMapping("/secure/sortPilotageBAS")
	public String sortPilotageBASAction(Model model) {		
		return sortPilotageBAS(model);
	}
	
    
	// Actions du bac à sable

	@RequestMapping("/secure/filesUploadBAS")
	public String filesUploadBASAction(Model model) {
		return filesUploadBAS(model);
	}

	/**
	 * Bouton retour arriere
	 * @param model
	 * @param phaseAExecuter
	 * @return
	 */
	@RequestMapping("/secure/undoBatch")
	public String undoBatchAction(Model model, TraitementPhase phaseAExecuter) {
		return undoBatch(model, phaseAExecuter);
	}
	

	/**
	 * Service correspondant au bouton réiniitaliser bac à sable
	 * @param model
	 * @return
	 */
	@RequestMapping("/secure/resetBAS")
	public String resetBASAction(Model model) {
		return resetBAS(model);
	}
	
	
	/**
	 * Bouton executer batch
	 * @param model
	 * @param phaseAExecuter
	 * @return
	 */
	@RequestMapping("/secure/executerBatch")
	public String executerBatchAction(Model model, TraitementPhase phaseAExecuter) {
		return executerBatch(model, phaseAExecuter);
	}
	
}