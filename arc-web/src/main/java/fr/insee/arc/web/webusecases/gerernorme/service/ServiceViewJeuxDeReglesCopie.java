package fr.insee.arc.web.webusecases.gerernorme.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.web.util.ConstanteBD;

@Controller
public class ServiceViewJeuxDeReglesCopie extends HubServiceGererNorme {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewJeuxDeReglesCopie.class);

	/**
	 * Action trigger by requesting the load rules of the register rule set to copy
	 * in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesChargementCopie(Model model) {
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewChargement.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewChargement.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the structurize rules of the register rule set
	 * to copy in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesNormageCopie(Model model) {

		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewNormage.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewNormage.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the control rules of the register rule set to
	 * copy in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesControleCopie(Model model) {

		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewControle.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewControle.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the filter rules of the register rule set to
	 * copy in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesFiltrageCopie(Model model) {

		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewFiltrage.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewFiltrage.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the map rules of the register rule set to copy
	 * in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesMappingCopie(Model model) {

		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewMapping.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewMapping.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String selectJeuxDeReglesExpressionCopie(Model model) {

		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_TABLE, this.viewExpression.getTable());
		this.viewJeuxDeReglesCopie.setCustomValue(SELECTED_RULESET_NAME, this.viewExpression.getSessionName());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String selectJeuxDeReglesCopie(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String copieJeuxDeRegles(Model model) throws ArcException {
		loggerDispatcher.info("Mon action pour copier un jeu de règles", LOGGER);
		// le jeu de regle à copier
		Map<String, ArrayList<String>> selectionOut = viewJeuxDeRegles.mapContentSelected();
		// le nouveau jeu de regle
		Map<String, ArrayList<String>> selectionIn = viewJeuxDeReglesCopie.mapContentSelected();
		HashMap<String, String> type = viewJeuxDeReglesCopie.mapHeadersType();
		if (!selectionIn.isEmpty()) {

			// columns found in all rules tables
			String inCommonColumns = new StringBuilder().append(ConstanteBD.ID_NORME.getValue())
					.append("," + ConstanteBD.PERIODICITE.getValue()).append("," + ConstanteBD.VALIDITE_INF.getValue())
					.append("," + ConstanteBD.VALIDITE_SUP.getValue()).append("," + ConstanteBD.VERSION.getValue())
					.toString();

			// specific columns = column of the table minus common tables minus id_regle
			// (rules generated id)
			ArcPreparedStatementBuilder getTableSpecificColumns = new ArcPreparedStatementBuilder();
			getTableSpecificColumns.append("\n SELECT string_agg(column_name,',') ");
			getTableSpecificColumns.append("\n FROM information_schema.columns c ");
			getTableSpecificColumns.append("\n WHERE table_schema||'.'||table_name ="
					+ getTableSpecificColumns.quoteText(this.getSelectedJeuDeRegle()));
			getTableSpecificColumns.append("\n AND column_name NOT IN ");
			getTableSpecificColumns.append(
					"\n ('" + inCommonColumns.replace(",", "','") + "','" + ConstanteBD.ID_REGLE.getValue() + "') ");

			String specificColumns = UtilitaireDao.get(poolName).getString(null, getTableSpecificColumns);

			// Build the copy query
			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

			requete.append("INSERT INTO " + this.getSelectedJeuDeRegle() + " ");
			requete.append("(");
			requete.append(inCommonColumns + "," + specificColumns);
			requete.append(")");

			requete.append("\n SELECT ");
			requete.append(String.join(",", requete.quoteText(selectionOut.get(ConstanteBD.ID_NORME.getValue()).get(0)),
					requete.quoteText(selectionOut.get(ConstanteBD.PERIODICITE.getValue()).get(0)),
					requete.quoteText(selectionOut.get(ConstanteBD.VALIDITE_INF.getValue()).get(0)) + "::date ",
					requete.quoteText(selectionOut.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0)) + "::date ",
					requete.quoteText(selectionOut.get(ConstanteBD.VERSION.getValue()).get(0))));
			requete.append("," + specificColumns);

			requete.append(" FROM " + this.getSelectedJeuDeRegle() + "  ");

			requete.append(" WHERE ");

			requete.append(String.join(" AND ", //
					// condition about id_norm
					ConstanteBD.ID_NORME.getValue() + requete.sqlEqual(
							selectionIn.get(ConstanteBD.ID_NORME.getValue()).get(0),
							type.get(ConstanteBD.ID_NORME.getValue())),
					ConstanteBD.PERIODICITE.getValue()
							// condition about PERIODICITE
							+ requete.sqlEqual(selectionIn.get(ConstanteBD.PERIODICITE.getValue()).get(0),
									type.get(ConstanteBD.PERIODICITE.getValue())),
					ConstanteBD.VALIDITE_INF.getValue()
							// condition about VALIDITE_INF
							+ requete.sqlEqual(selectionIn.get(ConstanteBD.VALIDITE_INF.getValue()).get(0),
									type.get(ConstanteBD.VALIDITE_INF.getValue())),
					ConstanteBD.VALIDITE_SUP.getValue()
							// condition about VALIDITE_SUP
							+ requete.sqlEqual(selectionIn.get(ConstanteBD.VALIDITE_SUP.getValue()).get(0),
									type.get(ConstanteBD.VALIDITE_SUP.getValue())),
					ConstanteBD.VERSION.getValue()
							// condition about VERSION
							+ requete.sqlEqual(selectionIn.get(ConstanteBD.VERSION.getValue()).get(0),
									type.get(ConstanteBD.VERSION.getValue()))

			));
			requete.append(" order by " + ConstanteBD.ID_REGLE.getValue() + " ;");

			// delete the current rules before the copy
			if (this.getSelectedJeuDeRegle().equals("arc.ihm_chargement_regle")) {
				emptyRuleTable(this.viewJeuxDeRegles, dataObjectService.getView(ViewEnum.IHM_CHARGEMENT_REGLE));
			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_normage_regle")) {
				emptyRuleTable(this.viewJeuxDeRegles, dataObjectService.getView(ViewEnum.IHM_NORMAGE_REGLE));
			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_controle_regle")) {
				emptyRuleTable(this.viewJeuxDeRegles, dataObjectService.getView(ViewEnum.IHM_CONTROLE_REGLE));
			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_filtrage_regle")) {
				emptyRuleTable(this.viewJeuxDeRegles, dataObjectService.getView(ViewEnum.IHM_FILTRAGE_REGLE));
			} else if (this.getSelectedJeuDeRegle().equals("arc.ihm_mapping_regle")) {
				emptyRuleTable(this.viewJeuxDeRegles, dataObjectService.getView(ViewEnum.IHM_MAPPING_REGLE));
			}

			// excute the copy
			try {
				UtilitaireDao.get("arc").executeRequest(null, requete);
			} catch (ArcException ex) {
				loggerDispatcher.error("Error in copieJeuxDeRegles", ex, LOGGER);
			}
			this.vObjectService.destroy(viewJeuxDeReglesCopie);
		} else {
			loggerDispatcher.info("No rule set choosed", LOGGER);
			this.viewJeuxDeRegles.setMessage("Please choose a ruleset");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
