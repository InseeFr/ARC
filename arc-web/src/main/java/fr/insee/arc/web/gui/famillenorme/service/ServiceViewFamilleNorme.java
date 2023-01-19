package fr.insee.arc.web.gui.famillenorme.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.serviceinteractif.ddi.DDIModeler;
import fr.insee.arc.core.serviceinteractif.ddi.DDIParser;
import fr.insee.arc.core.serviceinteractif.ddi.dao.DDIInsertDAO;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

@Service
public class ServiceViewFamilleNorme extends InteractorFamilleNorme {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewFamilleNorme.class);

	public String selectFamilleNorme(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addFamilleNorme(Model model) {
		// Clean up spaces
		String nomFamilleNorme = views.getViewFamilleNorme().getInputFieldFor(ID_FAMILLE);
		views.getViewFamilleNorme().setInputFieldFor(ID_FAMILLE, nomFamilleNorme.trim());
		return addLineVobject(model, RESULT_SUCCESS, views.getViewFamilleNorme());
	}

	public String deleteFamilleNorme(Model model) {

		try {
			ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
			query.append(this.vObjectService.deleteQuery(views.getViewFamilleNorme()));
			query.append(synchronizeRegleWithVariableMetier(
					views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0)));
			query.asTransaction();

			UtilitaireDao.get("arc").executeRequest(null, query);
		} catch (ArcException e) {
			this.views.getViewFamilleNorme().setMessage("La suppression des tables a échoué");
		}

		return deleteLineVobject(model, RESULT_SUCCESS, views.getViewFamilleNorme());
	}

	public String updateFamilleNorme(Model model) {
		return updateVobject(model, RESULT_SUCCESS, views.getViewFamilleNorme());
	}

	public String sortFamilleNorme(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewFamilleNorme());
	}

	public String downloadFamilleNorme(Model model, HttpServletResponse response) {

		Map<String, ArrayList<String>> selection = views.getViewFamilleNorme().mapContentSelected();

		if (!selection.isEmpty()) {

			String selectedFamille = selection.get(ID_FAMILLE).get(0);

			ArcPreparedStatementBuilder requeteTableMetier = new ArcPreparedStatementBuilder();
			requeteTableMetier.append("SELECT a.* ");
			requeteTableMetier.append("FROM arc.ihm_mod_table_metier a ");
			requeteTableMetier.append("WHERE " + ID_FAMILLE + "=");
			requeteTableMetier.appendQuoteText(selectedFamille);

			ArcPreparedStatementBuilder requeteVariableMetier = new ArcPreparedStatementBuilder();
			requeteVariableMetier.append("SELECT a.* ");
			requeteVariableMetier.append("FROM arc.ihm_mod_variable_metier a ");
			requeteVariableMetier.append("WHERE " + ID_FAMILLE + "=");
			requeteVariableMetier.appendQuoteText(selectedFamille);

			ArrayList<ArcPreparedStatementBuilder> queries = new ArrayList<>();
			queries.add(requeteTableMetier);
			queries.add(requeteVariableMetier);

			ArrayList<String> fileNames = new ArrayList<>();
			fileNames.add("modelTables");
			fileNames.add("modelVariables");

			this.vObjectService.download(views.getViewFamilleNorme(), response, fileNames, queries);
			return "none";
		} else {
			this.views.getViewFamilleNorme().setMessage("You didn't select anything");
			return generateDisplay(model, RESULT_SUCCESS);
		}

	}

	/**
	 * Import a xml ddi file into the norm family
	 * 
	 * @param model
	 * @param fileUploadDDI
	 * @return
	 */
	public String importDDI(Model model, MultipartFile fileUploadDDI) {
		loggerDispatcher.debug("importDDI", LOGGER);
		try {

			DDIModeler modeler = DDIParser.parse(fileUploadDDI.getInputStream());

			new DDIInsertDAO(this.dataObjectService).insertDDI(modeler);

		} catch (ArcException | IOException e) {
			this.views.getViewFamilleNorme().setMessage("DDI import failed");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
