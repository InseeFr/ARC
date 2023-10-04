package fr.insee.arc.web.gui.famillenorme.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.exception.ArcException;

@Service
public class ServiceViewTableMetier extends InteractorFamilleNorme {

	public String selectTableMetier(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addTableMetier(Model model) {
		if (isNomTableMetierValide(views.getViewTableMetier().mapInputFields().get(NOM_TABLE_METIER).get(0))) {
			this.vObjectService.insert(views.getViewTableMetier());
		} else {
			setMessageNomTableMetierInvalide();
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String deleteTableMetier(Model model) {

		try {
			String idFamilleSelected = views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0);
			// if family is selected
			if (idFamilleSelected != null) {
				dao.execQueryDeleteTableMetier(views.getViewTableMetier(), idFamilleSelected);
			}
		} catch (ArcException e) {
			this.views.getViewTableMetier().setMessage("familyManagement.delete.error");
		}
		// table metier must be rebuilt from scratch as some column might not exists anymore
		this.vObjectService.destroy(views.getViewTableMetier());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortTableMetier(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewTableMetier());
	}

	private void setMessageNomTableMetierInvalide() {
		this.views.getViewTableMetier().setMessage("familyManagement.table.error.invalidname");
		this.views.getViewTableMetier()
				.setMessageArgs(views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0));
	}

	private final boolean isNomTableMetierValide(String nomTable) {
		return isNomTableMetierValide(nomTable, TraitementPhase.MAPPING.toString().toLowerCase(),
				views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0));
	}

}
