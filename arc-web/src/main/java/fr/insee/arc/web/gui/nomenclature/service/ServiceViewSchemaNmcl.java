package fr.insee.arc.web.gui.nomenclature.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewSchemaNmcl extends InteractorNomenclature {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewSchemaNmcl.class);

	private static final String TYPE_COLONNE = "type_colonne";
	private static final String NOM_COLONNE = "nom_colonne";

	public String selectSchemaNmcl(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addSchemaNmcl(Model model) {

		// insert if type and colonne are valid
		if (isColonneValide(views.getViewSchemaNmcl().mapInputFields().get(NOM_COLONNE).get(0))
				&& isTypeValide(views.getViewSchemaNmcl().mapInputFields().get(TYPE_COLONNE).get(0))) {

			this.vObjectService.insert(views.getViewSchemaNmcl());
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String updateSchemaNmcl(Model model) {
		HashMap<String, ArrayList<String>> selection = views.getViewSchemaNmcl().mapContentAfterUpdate();
		if (!selection.isEmpty()) {
			boolean zeroErreur = true;

			loggerDispatcher.debug("nombre de valeurs : " + selection.get(NOM_COLONNE).size(), LOGGER);
			for (int i = 0; i < selection.get(NOM_COLONNE).size(); i++) {
				String nomColonne = selection.get(NOM_COLONNE).get(i);
				String typeColonne = selection.get(TYPE_COLONNE).get(i);
				
				loggerDispatcher.debug("test colonne : " + nomColonne + " - " + typeColonne, LOGGER);
				if (!isColonneValide(nomColonne) || !isTypeValide(typeColonne)) {
					zeroErreur = false;
					break;
				}
			}
			if (zeroErreur) {
				this.vObjectService.update(views.getViewSchemaNmcl());
			}
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortSchemaNmcl(Model model) {
		this.vObjectService.sort(views.getViewSchemaNmcl());
		return basicAction(model, RESULT_SUCCESS);
	}

	public String deleteSchemaNmcl(Model model) {
		this.vObjectService.delete(views.getViewSchemaNmcl());
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * check the column name return a message if the name is not valid for database
	 * 
	 * @param typeColonne
	 * @return
	 */
	private boolean isColonneValide(String nomColonne) {
		boolean result = dao.isColonneValide(nomColonne);
		if (!result) {
			this.views.getViewSchemaNmcl().setMessage("nmclManagement.schema.invalidColumnName");
			this.views.getViewSchemaNmcl().setMessageArgs(nomColonne);
		}
		return result;
	}

	/**
	 * check the column type return a message if the type is not valid for database
	 * 
	 * @param typeColonne
	 * @return
	 */
	private boolean isTypeValide(String typeColonne) {
		boolean result = dao.isTypeValide(typeColonne);
		if (!result) {
			this.views.getViewSchemaNmcl().setMessage("nmclManagement.schema.invalidTypeName");
			this.views.getViewSchemaNmcl().setMessageArgs(typeColonne);
		}
		return result;
	}

}
