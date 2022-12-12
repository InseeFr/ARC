package fr.insee.arc.web.webusecases.gerernorme.controller;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.web.webusecases.gerernorme.HubControllerGererNorme;

@Component
public class ControllerViewExpression extends HubControllerGererNorme {

	@RequestMapping("/selectExpression")
	public String selectExpressionAction(Model model) {
		return selectExpression(model);
	}
	
	@RequestMapping("/addExpression")
	public String addExpressionAction(Model model) {
		return addExpression(model);
	}

	@RequestMapping("/updateExpression")
	public String updateExpressionAction(Model model) {
		return updateExpression(model);
	}

	@RequestMapping("/sortExpression")
	public String sortExpressionAction(Model model) {
		return sortExpression(model);
	}
	
	@RequestMapping("/deleteExpression")
	public String deleteExpressionAction(Model model) {
		return deleteExpression(model);
	}
	
	@RequestMapping("/importExpression")
	public String importExpressionAction(Model model, MultipartFile fileUploadExpression) {
		return importFiltrage(model, fileUploadExpression);
	}

	/**
	 * Clean the expressions. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderExpression")
	public String viderExpressionAction(Model model) {
		return viderExpression(model);
	}

	
}
