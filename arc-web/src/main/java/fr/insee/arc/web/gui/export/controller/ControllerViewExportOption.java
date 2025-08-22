package fr.insee.arc.web.gui.export.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.export.service.ServiceViewExportOption;

@Controller
public class ControllerViewExportOption extends ServiceViewExportOption {

	/**
	 * Action trigger when the table of control rules is request or refresh. Update
	 * the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/selectExportOption")
	public String selectExportOptionAction(Model model) {
		return selectExportOption(model);
	}

	/**
	 * Action trigger by adding a control rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/addExportOption")
	public String addExportOptionAction(Model model) {
		return addExportOption(model);
	}

	/**
	 * Action trigger by deleting a control rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return
	 */
	@RequestMapping("/secure/deleteExportOption")
	public String deleteExportOptionAction(Model model) {
		return deleteExportOption(model);
	}

	/**
	 * Action trigger by updating some control rules in the GUI. Update the GUI and
	 * the database. Before insertion in data base check if the news rules are
	 * coherents
	 * 
	 * @return
	 */
	@RequestMapping("/secure/updateExportOption")
	public String updateExportOptionAction(Model model) {
		return updateExportOption(model);
	}

	/**
	 * Action trigger by sorting a control rules in the GUI. Update the GUI .
	 * 
	 * @return
	 */
	@RequestMapping("/secure/sortExportOption")
	public String sortExportOptionAction(Model model) {
		return sortExportOption(model);
	}

}
