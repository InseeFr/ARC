package fr.insee.arc.web.webusecases.gererfamillenorme.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

@Service
public class ServiceViewTableMetier extends HubServiceGererFamilleNorme {


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

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(this.vObjectService.deleteQuery(views.getViewTableMetier()));
		query.append(synchronizeRegleWithVariableMetier(views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0)));
		query.asTransaction();

		try {
			UtilitaireDao.get("arc").executeRequest(null, query);
		} catch (ArcException e) {
			this.views.getViewTableMetier().setMessage("La suppression des tables a échoué");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortTableMetier(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewTableMetier());
	}


	private void setMessageNomTableMetierInvalide() {
		this.views.getViewTableMetier().setMessage("familyManagement.table.error.invalidname");
		this.views.getViewTableMetier().setMessageArgs(views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0));
	}
	

	private final boolean isNomTableMetierValide(String nomTable) {
		return isNomTableMetierValide(nomTable, TraitementPhase.MAPPING.toString().toLowerCase(),
				views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0));
	}


}
