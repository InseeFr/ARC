package fr.insee.arc.web.gui.norme.service;

import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.utils.exception.ArcException;

@Service
public class ServiceViewJeuxDeReglesCopie extends InteractorNorme {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewJeuxDeReglesCopie.class);

	/**
	 * Action trigger by requesting the load rules of the register rule set to copy
	 * in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesChargementCopie(Model model) {
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_TABLE, this.views.getViewChargement().getTable());
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_NAME, this.views.getViewChargement().getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the structurize rules of the register rule set
	 * to copy in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesNormageCopie(Model model) {

		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_TABLE, this.views.getViewNormage().getTable());
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_NAME, this.views.getViewNormage().getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the control rules of the register rule set to
	 * copy in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesControleCopie(Model model) {

		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_TABLE, this.views.getViewControle().getTable());
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_NAME, this.views.getViewControle().getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the map rules of the register rule set to copy
	 * in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesMappingCopie(Model model) {

		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_TABLE, this.views.getViewMapping().getTable());
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_NAME, this.views.getViewMapping().getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String selectJeuxDeReglesExpressionCopie(Model model) {

		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_TABLE, this.views.getViewExpression().getTable());
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_NAME, this.views.getViewExpression().getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String selectJeuxDeReglesCopie(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String copieJeuxDeRegles(Model model) {
		loggerDispatcher.info("Mon action pour copier un jeu de règles", LOGGER);
		
		// le jeu de regle à copier
		Map<String, ArrayList<String>> selectionOut = views.getViewJeuxDeRegles().mapContentSelected();
		// le nouveau jeu de regle
		Map<String, ArrayList<String>> selectionIn = views.getViewJeuxDeReglesCopie().mapContentSelected();
		
		if (!selectionOut.isEmpty() && !selectionIn.isEmpty()) {

			try {
			dao.execQueryCopieJeuxDeRegles(views.getViewJeuxDeRegles(), views.getViewJeuxDeReglesCopie(), this.getSelectedJeuDeRegle());
			
			this.vObjectService.destroy(views.getViewJeuxDeReglesCopie());
		} catch (ArcException ex) {
			loggerDispatcher.error("Error in copieJeuxDeRegles", ex, LOGGER);
		}
		} else {
			loggerDispatcher.info("No rule set choosed", LOGGER);
			this.views.getViewJeuxDeRegles().setMessage("normManagement.copyRuleset.noSelection");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
