package fr.insee.arc.web.gui.norme.service;

import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;

@Service
public class ServiceViewFiltrage extends InteractorNorme {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewFiltrage.class);

	/**
	 * Action trigger when the table of map rules is request or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	public String selectFiltrage(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by updating a filter rule in the GUI. Update the GUI and the
	 * database. Before inserting, the rules are checked
	 * 
	 * @return
	 */
	public String updateFiltrage(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.views.getViewFiltrage());
	}

	/**
	 * Action trigger by deleting a structurize rule in the GUI. Update the GUI and
	 * the database
	 * 
	 * @return
	 */
	public String deleteFiltrage(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.views.getViewFiltrage());
	}

	/**
	 * Action trigger by sorting the filter rules in the GUI. Update the GUI
	 * 
	 * @return
	 */
	public String sortFiltrage(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.views.getViewFiltrage());
	}

	/**
	 * Clean the filter rules. Update GUI and database
	 * 
	 * @return
	 */
	public String viderFiltrage(Model model) {

		emptyRuleTable(views.getViewJeuxDeRegles(), dataObjectService.getView(ViewEnum.IHM_FILTRAGE_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);

	}

	/**
	 * Action trigger by uploading a filter rule file
	 * 
	 * @return
	 */
	public String importFiltrage(Model model, MultipartFile fileUploadFilter) {

		uploadFileRule(this.views.getViewFiltrage(), this.views.getViewJeuxDeRegles(), fileUploadFilter);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action initializing the filter rules
	 * 
	 * @return
	 */
	public String preGenererRegleFiltrage(Model model) {
		try {
			Map<String, ArrayList<String>> selection = views.getViewJeuxDeRegles().mapContentSelected();

			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			requete.append("INSERT INTO " + this.views.getViewFiltrage().getTable())
					.append("  " + Format.stringListe(this.views.getViewFiltrage().getHeadersDLabel()))
					.append("  SELECT (SELECT coalesce(max(id_regle),1) FROM " + this.views.getViewFiltrage().getTable()
							+ ")+row_number() over () ,")
					.append("  " + requete.quoteText(selection.get("id_norme").get(0)) + ", ")
					.append("  " + requete.quoteText(selection.get("validite_inf").get(0)) + "::date, ")
					.append("  " + requete.quoteText(selection.get("validite_sup").get(0)) + "::date, ")
					.append("  " + requete.quoteText(selection.get("version").get(0)) + ", ")
					.append("  " + requete.quoteText(selection.get("periodicite").get(0)) + ", ").append("  null,")//
					.append("  null;");

			UtilitaireDao.get("arc").executeRequest(null, requete);
		} catch (ArcException e) {
			loggerDispatcher.error(String.format("Error in preGenererRegleFiltrage : %s", e.toString()), LOGGER);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
