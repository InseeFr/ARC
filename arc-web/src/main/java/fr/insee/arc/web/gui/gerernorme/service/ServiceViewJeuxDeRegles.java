package fr.insee.arc.web.gui.gerernorme.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.util.ConstanteBD;
import fr.insee.arc.web.util.VObject;

@Service
public class ServiceViewJeuxDeRegles extends HubServiceGererNorme {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewJeuxDeRegles.class);


	/**
	 * Action trigger by selecting a rule set in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	public String selectRuleSet(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a rule set in the GUI. Cannot add a rule set in
	 * production state. Update the GUI and the database
	 * 
	 * @return success
	 */
	public String addRuleSet(Model model) {
		HashMap<String, ArrayList<String>> selection = views.getViewJeuxDeRegles().mapInputFields();
		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
				this.views.getViewJeuxDeRegles().setMessage("Caution, cannot add a rule set in the PRODUCTION state");
			} else {
				this.vObjectService.insert(views.getViewJeuxDeRegles());
			}
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by deleting a rule set in the GUI. Cannot delete a rule state
	 * in the PRODUCTIONS state. Update the GUI and the database
	 * 
	 * @return sucess
	 */
	public String deleteRuleSet(Model model) {
		
		// Get the selection
		Map<String, ArrayList<String>> selection = views.getViewJeuxDeRegles().mapContentSelected();
		if (!selection.isEmpty()) {
			String etat = selection.get("etat").get(0);
			loggerDispatcher.info("State to delete : " + etat, LOGGER);
			// Check production state. If yes cancel the delete and send a message to the
			// user
			if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
				this.views.getViewJeuxDeRegles().setMessage("Caution, cannot delete a rule set in the PRODUCTION state");
			} else {
				this.vObjectService.delete(views.getViewJeuxDeRegles());
			}
		} else {
			this.views.getViewJeuxDeRegles().setMessage("You didn't select anything");
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by updating a rule set in the GUI. Update the GUI and the
	 * database.
	 * 
	 * If the rule set is send to production, send a dummy file in prod which
	 * trigger the initialisation batch.
	 * 
	 * @return success
	 */
	public String updateRuleSet(Model model) {
		HashMap<String, ArrayList<String>> selection = views.getViewJeuxDeRegles().mapContentSelected();

		// on les crée dans tous les environnements et tous les entrepots
		// (ca evite les erreurs et car ca ne spécialise aucun environnement dans un
		// role à priori)

		if (!selection.isEmpty()) {
			for (int i = 0; i < selection.get("etat").size(); i++) {
				String etat = selection.get("etat").get(i);
				if (ConstanteBD.ARC_PROD.getValue().equals(etat)) {
					sendRuleSetToProduction(this.views.getViewJeuxDeRegles(),
							dataObjectService.getView(ViewEnum.PILOTAGE_BATCH)
							
							);
				}
			}

			this.vObjectService.update(views.getViewJeuxDeRegles());

		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by sorting the ruleset in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	public String sortRuleSet(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.views.getViewJeuxDeRegles());
	}

	/**
	 * Get all the rules from on rule set, export them in CSV, zip and send to user
	 * 
	 * @return
	 */
	public String downloadJeuxDeRegles(Model model, HttpServletResponse response) {
		Map<String, ArrayList<String>> selection = views.getViewJeuxDeRegles().mapContentSelected();
		if (!selection.isEmpty()) {
			ArcPreparedStatementBuilder requeteRegleChargement = new ArcPreparedStatementBuilder();
			requeteRegleChargement.append(recupRegle(this.views.getViewJeuxDeRegles(),
					dataObjectService.getView(ViewEnum.IHM_CHARGEMENT_REGLE)
					));
			ArcPreparedStatementBuilder requeteRegleNormage = new ArcPreparedStatementBuilder();
			requeteRegleNormage.append(recupRegle(this.views.getViewJeuxDeRegles(),
					dataObjectService.getView(ViewEnum.IHM_NORMAGE_REGLE)));
			ArcPreparedStatementBuilder requeteRegleControle = new ArcPreparedStatementBuilder();
			requeteRegleControle.append(recupRegle(this.views.getViewJeuxDeRegles(),
					dataObjectService.getView(ViewEnum.IHM_CONTROLE_REGLE)));
			ArcPreparedStatementBuilder requeteRegleMapping = new ArcPreparedStatementBuilder();
			requeteRegleMapping.append(recupRegle(this.views.getViewJeuxDeRegles(),
					dataObjectService.getView(ViewEnum.IHM_MAPPING_REGLE)));
			ArcPreparedStatementBuilder requeteRegleFiltrage = new ArcPreparedStatementBuilder();
			requeteRegleFiltrage.append(recupRegle(this.views.getViewJeuxDeRegles(),
					dataObjectService.getView(ViewEnum.IHM_FILTRAGE_REGLE)));
			ArcPreparedStatementBuilder requeteRegleExpression = new ArcPreparedStatementBuilder();
			requeteRegleExpression.append(recupRegle(this.views.getViewJeuxDeRegles(),
					dataObjectService.getView(ViewEnum.IHM_EXPRESSION)));

			ArrayList<String> fileNames = new ArrayList<>();
			fileNames.add("Rules_load");
			fileNames.add("Rules_structurize");
			fileNames.add("Rules_control");
			fileNames.add("Rules_mapping");
			fileNames.add("Rules_filter");
			fileNames.add("Rules_expression");
			this.vObjectService.download(views.getViewJeuxDeRegles(), response, fileNames
					, new ArrayList<>(
							Arrays.asList(
									requeteRegleChargement
									, requeteRegleNormage
									, requeteRegleControle
									, requeteRegleMapping
									, requeteRegleFiltrage
									, requeteRegleExpression
									)
							)
					);
			return "none";
		} else {
			this.views.getViewJeuxDeRegles().setMessage("You didn't select anything");
			return generateDisplay(model, RESULT_SUCCESS);
		}

	}
	
	/**
	 * Send a rule set to production.
	 */
	public void sendRuleSetToProduction(VObject viewRulesSet, String theTable) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");
		Date dNow = new Date();
		loggerDispatcher.warn("Rule set send to production", LOGGER);

		try {
			
			ArcPreparedStatementBuilder requete= new ArcPreparedStatementBuilder();
			requete.append("update " + theTable + " set last_init='"+ dateFormat.format(dNow) + "', operation=case when operation='R' then 'O' else operation end;");
			
			UtilitaireDao.get("arc").executeRequest(null, requete);
			viewRulesSet.setMessage("Go to production registered");

		} catch (ArcException e) {
			viewRulesSet.setMessage("Error in the go to production");
			LoggerHelper.warn(LOGGER, "Error in the go to production");

		}
	}
	

	/**
	 * Return the SQL to get all the rules bond to a rule set. It suppose the a rule
	 * set is selected
	 * 
	 * @param viewRulesSet : the Vobject containing the rules
	 * @param table        : the sql to get the rules in the database
	 * @return an sql query to get all the rules bond to a rule set
	 */
	public ArcPreparedStatementBuilder recupRegle(VObject viewRulesSet, String table) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		Map<String, ArrayList<String>> selection = viewRulesSet.mapContentSelected();
		HashMap<String, String> type = viewRulesSet.mapHeadersType();
        requete.append("select * from " + table + " ");
        whereRuleSetEquals(requete, selection, type);
		return requete;
	}

	
}
