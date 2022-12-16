package fr.insee.arc.web.webusecases.gerernorme.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ViewEnum;

@Service
public class ServiceViewChargement extends HubServiceGererNorme {

	/**
	 * Action trigger when the table of load rule is asked or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	public String selectChargement(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger when the table of load rule is asked or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	public String addChargement(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.views.getViewChargement());
	}

	/**
	 * Action trigger by adding a load rule in the GUI. Update the GUI and the
	 * database
	 *
	 * @return
	 */
	public String deleteChargement(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.views.getViewChargement());
	}

	/**
	 * Action trigger by updating a load rule in the GUI. Update the GUI and the
	 * database.
	 * 
	 * @return
	 */
	public String updateChargement(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.views.getViewChargement());
	}

	/**
	 * Action trigger by updating a load rule in the GUI. Update the GUI and the
	 * database.
	 * 
	 * @return
	 */
	public String sortChargement(Model model) {

		this.vObjectService.sort(views.getViewChargement());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by importing load rules in the GUI. Update the GUI and the
	 * database.
	 * 
	 * @return
	 */
	public String importChargement(Model model, MultipartFile fileUploadLoad) {
		uploadFileRule(views.getViewChargement(), views.getViewJeuxDeRegles(), fileUploadLoad);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the loading rules. Update GUI and database
	 * 
	 * @return
	 */
	public String viderChargement(Model model) {

		emptyRuleTable(views.getViewJeuxDeRegles(), dataObjectService.getView(ViewEnum.IHM_CHARGEMENT_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
