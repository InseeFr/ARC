package fr.insee.arc.web.gui.norme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.web.gui.norme.service.ServiceViewExpression;

@Controller
public class ControllerViewExpression extends ServiceViewExpression {

	@RequestMapping("/secure/selectExpression")
	public String selectExpressionAction(Model model) {
		return selectExpression(model);
	}

	@RequestMapping("/secure/addExpression")
	public String addExpressionAction(Model model) {
		return addExpression(model);
	}

	@RequestMapping("/secure/updateExpression")
	public String updateExpressionAction(Model model) {
		return updateExpression(model);
	}

	@RequestMapping("/secure/sortExpression")
	public String sortExpressionAction(Model model) {
		return sortExpression(model);
	}

	@RequestMapping("/secure/deleteExpression")
	public String deleteExpressionAction(Model model) {
		return deleteExpression(model);
	}

	@RequestMapping("/secure/importExpression")
	public String importExpressionAction(Model model, MultipartFile fileUploadExpression) {
		return importExpression(model, fileUploadExpression);
	}

	/**
	 * Clean the expressions. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/secure/viderExpression")
	public String viderExpressionAction(Model model) {
		return viderExpression(model);
	}

}
