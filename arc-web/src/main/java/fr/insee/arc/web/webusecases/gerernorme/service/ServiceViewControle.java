package fr.insee.arc.web.webusecases.gerernorme.service;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ViewEnum;

@Controller
public class ServiceViewControle extends HubServiceGererNorme {

	/**
	 * Action trigger when the table of control rules is request or refresh. Update
	 * the GUI
	 * 
	 * @return success
	 */
	public String selectControle(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by adding a control rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return success
	 */
	public String addControle(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.views.getViewControle());
	}

	/**
	 * Action trigger by deleting a control rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return
	 */
	public String deleteControle(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.views.getViewControle());
	}

	/**
	 * Action trigger by updating some control rules in the GUI. Update the GUI and
	 * the database. Before insertion in data base check if the news rules are
	 * coherents
	 * 
	 * @return
	 */
	public String updateControle(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.views.getViewControle());
	}

	/**
	 * Action trigger by sorting a control rules in the GUI. Update the GUI .
	 * 
	 * @return
	 */
	public String sortControle(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.views.getViewControle());
	}

	/**
	 * Action trigger by uploading a file with rule
	 * 
	 * @return
	 * @throws IOException
	 */
	public String importControle(Model model, MultipartFile fileUploadControle) {
		uploadFileRule(views.getViewControle(), views.getViewJeuxDeRegles(), fileUploadControle);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the control rules. Update GUI and database
	 * 
	 * @return
	 */
	public String viderControle(Model model) {

		emptyRuleTable(this.views.getViewJeuxDeRegles(), dataObjectService.getView(ViewEnum.IHM_CONTROLE_REGLE));
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
