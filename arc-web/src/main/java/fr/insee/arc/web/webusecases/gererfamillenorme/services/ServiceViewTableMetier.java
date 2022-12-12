package fr.insee.arc.web.webusecases.gererfamillenorme.services;

import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class ServiceViewTableMetier extends GererFamilleNormeService {


	public String selectTableMetier(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addTableMetier(Model model) {
		if (isNomTableMetierValide(viewTableMetier.mapInputFields().get(NOM_TABLE_METIER).get(0))) {
			this.vObjectService.insert(viewTableMetier);
		} else {
			setMessageNomTableMetierInvalide();
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String deleteTableMetier(Model model) {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(this.vObjectService.deleteQuery(viewTableMetier));
		query.append(synchronizeRegleWithVariableMetier(viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0)));
		query.asTransaction();

		try {
			UtilitaireDao.get("arc").executeRequest(null, query);
		} catch (ArcException e) {
			this.viewTableMetier.setMessage("La suppression des tables a échoué");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortTableMetier(Model model) {
		return sortVobject(model, RESULT_SUCCESS, getViewTableMetier());
	}


	private void setMessageNomTableMetierInvalide() {
		this.viewTableMetier.setMessage("familyManagement.table.error.invalidname");
		this.viewTableMetier.setMessageArgs(viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0));
	}
	

	private final boolean isNomTableMetierValide(String nomTable) {
		return isNomTableMetierValide(nomTable, TraitementPhase.MAPPING.toString().toLowerCase(),
				viewFamilleNorme.mapContentSelected().get(ID_FAMILLE).get(0));
	}


}
