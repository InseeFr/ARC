package fr.insee.arc.web.gui.norme.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.exception.ArcException;

@Service
public class ServiceViewChargement extends InteractorNorme {

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
		return sortVobject(model, RESULT_SUCCESS, this.views.getViewChargement());
	}

	/**
	 * Action trigger by importing load rules in the GUI. Update the GUI and the
	 * database.
	 * 
	 * @return
	 */
	public String importChargement(Model model, MultipartFile fileUploadLoad) {
		trackThisAction();
		dao.uploadFileRule(views.getViewChargement(), views.getViewJeuxDeRegles(), fileUploadLoad);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the loading rules. Update GUI and database
	 * 
	 * @return
	 * @throws ArcException 
	 */
	public String viderChargement(Model model) {
		trackThisAction();
		try {
			dao.emptyRuleTable(views.getViewJeuxDeRegles(), dataObjectService.getView(ViewEnum.IHM_CHARGEMENT_REGLE));
		} catch (ArcException e) {
			e.logFullException();
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
