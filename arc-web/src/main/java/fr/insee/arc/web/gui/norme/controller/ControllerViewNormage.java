package fr.insee.arc.web.gui.norme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.web.gui.norme.service.ServiceViewNormage;

@Controller
public class ControllerViewNormage extends ServiceViewNormage {

	/**
	 * Action trigger by importing structurize rules in the GUI. Update the GUI and
	 * the database.
	 * 
	 * @return
	 */
	@RequestMapping("/secure/importNormage")
	public String importNormageAction(Model model, MultipartFile fileUploadStructurize) {
		return importNormage(model, fileUploadStructurize);
	}

	/**
	 * Action trigger when the table of structuize rules is request or refresh.
	 * Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/selectNormage")
	public String selectNormageAction(Model model) {
		return selectNormage(model);
	}

	/**
	 * Action trigger by adding a structurize rule in the GUI. Update the GUI and
	 * the database
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/addNormage")
	public String addNormageAction(Model model) {
		return addNormage(model);
	}

	/**
	 * Action trigger by deleting a structurize rule in the GUI. Update the GUI and
	 * the database
	 * 
	 * @return
	 */
	@RequestMapping("/secure/deleteNormage")
	public String deleteNormageAction(Model model) {
		return deleteNormage(model);
	}

	/**
	 * Action trigger by updating the structurize rule in the GUI. Update the GUI
	 * and the database
	 * 
	 * @return
	 */
	@RequestMapping("/secure/updateNormage")
	public String updateNormageAction(Model model) {
		return updateNormage(model);
	}

	/**
	 * Action trigger by sorting the structurize rule in the GUI. Update the GUI.
	 * 
	 * @return
	 */

	@RequestMapping("/secure/sortNormage")
	public String sortNormageAction(Model model) {
		return sortNormage(model);
	}
	
	/**
	 * Clean the structure rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/secure/viderNormage")
	public String viderNormageAction(Model model) {
		return viderNormage(model);
	}

	
}
