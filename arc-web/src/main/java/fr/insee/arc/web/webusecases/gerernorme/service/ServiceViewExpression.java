package fr.insee.arc.web.webusecases.gerernorme.service;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;

@Controller
public class ServiceViewExpression extends HubServiceGererNorme {

	public String selectExpression(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addExpression(Model model) {
		String exprNameHeader = ColumnEnum.EXPR_NOM.getColumnName();

		// trim the inserted expression
		viewExpression.setInputFieldFor(exprNameHeader, viewExpression.getInputFieldFor(exprNameHeader).trim());

		return addLineVobject(model, RESULT_SUCCESS, this.viewExpression);
	}

	public String updateExpression(Model model) {
		return updateVobject(model, RESULT_SUCCESS, this.viewExpression);
	}

	public String sortExpression(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewExpression);
	}

	public String deleteExpression(Model model) {
		return deleteLineVobject(model, RESULT_SUCCESS, this.viewExpression);
	}

	public String importExpression(Model model, MultipartFile fileUploadExpression) {
		uploadFileRule(getViewExpression(), viewJeuxDeRegles, fileUploadExpression);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Clean the expressions. Update GUI and database
	 * 
	 * @return
	 */
	public String viderExpression(Model model) {

		emptyRuleTable(this.viewJeuxDeRegles, dataObjectService.getView(ViewEnum.IHM_EXPRESSION));
		return generateDisplay(model, RESULT_SUCCESS);

	}

}
