package fr.insee.arc.web.gui.pilotage.service;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p0initialisation.metadata.SynchronizeRulesAndMetadataOperation;
import fr.insee.arc.utils.exception.ArcException;

@Service
public class ServiceViewPilotageProd extends InteractorPilotage {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewPilotageProd.class);

	
	/**
	 * Service permettant de visualiser l'état du batch en production
	 * @param model
	 * @param request
	 * @return
	 */
    public String informationInitialisationPROD(Model model, HttpServletRequest request) {
    	try {
			String time = dao.execQueryTime();
			String state = dao.execQueryState();
			state = messageSource.getMessage("managementSandbox.batch.status." + state, null, request.getLocale());
    		this.views.getViewPilotageBAS().setMessage("managementSandbox.batch.status");
    		this.views.getViewPilotageBAS().setMessageArgs(state, time);

		} catch (ArcException e) {
			loggerDispatcher.error("Error in informationInitialisationPROD", e, LOGGER);
		}
    	return generateDisplay(model, RESULT_SUCCESS);
    }

	/**
	 * Service pour retarder l'exécution automatique de la phase d'iniitalisation en production
	 * @param model
	 * @return
	 */
    public String retarderBatchInitialisationPROD(Model model) {
    	try {
			dao.execQueryDelayBatchTime();
			String time = dao.execQueryTime();
			this.views.getViewPilotageBAS().setMessage("managementSandbox.batch.init.time");
			this.views.getViewPilotageBAS().setMessageArgs(time);

			
		} catch (ArcException e) {
			loggerDispatcher.error("Error in retarderInitialisationPROD", e, LOGGER);
		}
    	
    	return generateDisplay(model, RESULT_SUCCESS);
    }
    
    /**
     * Service correspondant au bouton de demande immédiate de phase d'initialisation en production
     * @param model
     * @return
     */
    public String demanderBatchInitialisationPROD(Model model) {
    	
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			dao.execQueryForwardBatchTime();
			String time = dao.execQueryTime();
			this.views.getViewPilotageBAS().setMessage("managementSandbox.batch.init.time");
			this.views.getViewPilotageBAS().setMessageArgs(time);

			
		} catch (ArcException e) {
			loggerDispatcher.error("Error in demanderBatchInitialisationPROD", e, LOGGER);
		}
    	
    	return generateDisplay(model, RESULT_SUCCESS);
    }
    
    /**
     * Service correspondant à l'activation du batch de production
     * @param model
     * @return
     */
    public String toggleOnPROD(Model model) {
    	try {
			dao.execQueryToggleOn();
			this.views.getViewPilotageBAS().setMessage("managementSandbox.batch.status.switch.on");
		} catch (ArcException e) {
			loggerDispatcher.error("Error in toggleOnPROD", e, LOGGER);
		}
    	return generateDisplay(model, RESULT_SUCCESS);
    }

    
    /**
     * Service correspondant à la désactivation du batch de production
     * @param model
     * @return
     */
    public String toggleOffPROD(Model model) {
    	try {
    		dao.execQueryToggleOff();
			this.views.getViewPilotageBAS().setMessage("managementSandbox.batch.status.switch.off");
		} catch (ArcException e) {
			loggerDispatcher.error("Error in toggleOffPROD", e, LOGGER);
		}
    	return generateDisplay(model, RESULT_SUCCESS);
    }
	
    /**
     * user is able to copy the rules fast to a production environment
     * @param model
     * @return
     */
    public String applyRulesProd(Model model) {
    	try {
    		new SynchronizeRulesAndMetadataOperation(new Sandbox(null, getBacASable())).copyMetadataAllNods();
    	}
		catch (ArcException e)
		{
			this.views.getViewPilotageBAS().setMessage("managementSandbox.batch.update.error");
			return generateDisplay(model, RESULT_SUCCESS);
		}
    	return generateDisplay(model, RESULT_SUCCESS);
    }
    
    
}
