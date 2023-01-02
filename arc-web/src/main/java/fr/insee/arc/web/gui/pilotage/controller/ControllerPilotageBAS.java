package fr.insee.arc.web.gui.pilotage.controller;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.pilotage.service.ServiceViewPilotageBAS;

@Controller
public class ControllerPilotageBAS extends ServiceViewPilotageBAS {
	
	/**
	 * Entering sandbox monitoring from main menu build the database and filesystem
	 * 
	 * @return
	 */
	@RequestMapping("/enterPilotageBAS")
	public String enterPilotageBASAction(Model model) {
		return enterPilotageBAS(model);
	}

	@PostMapping(value = {"/selectPilotageBAS"}
	)
	public String selectPilotageBASAction(Model model) {
		return selectPilotageBAS(model);
	}
	
	@PostMapping("/updateEnvDescription")
	public String updateEnvDescriptionAction(Model model) {
		return updateEnvDescription(model);
	}
	
	@RequestMapping("/sortPilotageBAS")
	public String sortPilotageBASAction(Model model) {		
		return sortPilotageBAS(model);
	}
	
    
	// Actions du bac à sable

	@RequestMapping("/filesUploadBAS")
	public String filesUploadBASAction(Model model) {
		return filesUploadBAS(model);
	}

	/**
	 * Bouton retour arriere
	 * @param model
	 * @param phaseAExecuter
	 * @return
	 */
	@RequestMapping("/undoBatch")
	public String undoBatchAction(Model model, TraitementPhase phaseAExecuter) {
		return undoBatch(model, phaseAExecuter);
	}
	

	/**
	 * Service correspondant au bouton réiniitaliser bac à sable
	 * @param model
	 * @return
	 */
	@RequestMapping("/resetBAS")
	public String resetBASAction(Model model) {
		return resetBAS(model);
	}
	
	
	/**
	 * Bouton executer batch
	 * @param model
	 * @param phaseAExecuter
	 * @return
	 */
	@RequestMapping("/executerBatch")
	public String executerBatchAction(Model model, TraitementPhase phaseAExecuter) {
		return executerBatch(model, phaseAExecuter);
	}
	
}