package fr.insee.arc.web.gui.norme.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ViewEnum;

@Service
public class ServiceViewExpression extends InteractorNorme {

	public String selectExpression(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addExpression(Model model) {
		return addLineVobject(model, RESULT_SUCCESS, this.views.getViewExpression());
	}

	public String updateExpression(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.views.getViewExpression());
	}

	public String sortExpression(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.views.getViewExpression());
	}

	public String deleteExpression(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.views.getViewExpression());
	}

	public String importExpression(Model model, MultipartFile fileUploadExpression) {
		uploadFileRule(views.getViewExpression(), views.getViewJeuxDeRegles(), fileUploadExpression);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the expressions. Update GUI and database
	 * 
	 * @return
	 */
	public String viderExpression(Model model) {

		emptyRuleTable(this.views.getViewJeuxDeRegles(), dataObjectService.getView(ViewEnum.IHM_EXPRESSION));
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
