package fr.insee.arc.web.gui.norme.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ViewEnum;

@Service
public class ServiceViewNormage extends InteractorNorme {

	/**
	 * Action trigger by importing structurize rules in the GUI. Update the GUI and
	 * the database.
	 * 
	 * @return
	 */
	public String importNormage(Model model, MultipartFile fileUploadStructurize) {

		uploadFileRule(views.getViewNormage(), views.getViewJeuxDeRegles(), fileUploadStructurize);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger when the table of structuize rules is request or refresh.
	 * Update the GUI
	 * 
	 * @return success
	 */
	public String selectNormage(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a structurize rule in the GUI. Update the GUI and
	 * the database
	 * 
	 * @return success
	 */
	public String addNormage(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.views.getViewMapping());
	}

	/**
	 * Action trigger by deleting a structurize rule in the GUI. Update the GUI and
	 * the database
	 * 
	 * @return
	 */
	public String deleteNormage(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.views.getViewMapping());
	}

	/**
	 * Action trigger by updating the structurize rule in the GUI. Update the GUI
	 * and the database
	 * 
	 * @return
	 */
	public String updateNormage(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.views.getViewMapping());
	}

	/**
	 * Action trigger by sorting the structurize rule in the GUI. Update the GUI.
	 * 
	 * @return
	 */

	public String sortNormage(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.views.getViewMapping());
	}

	/**
	 * Clean the structure rules. Update GUI and database
	 * 
	 * @return
	 */
	public String viderNormage(Model model) {

		emptyRuleTable(this.views.getViewJeuxDeRegles(), dataObjectService.getView(ViewEnum.IHM_NORMAGE_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
