package fr.insee.arc.web.gui.norme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.web.gui.norme.service.ServiceViewChargement;

@Controller
public class ControllerViewChargement extends ServiceViewChargement {

	/**
	 * Action trigger when the table of load rule is asked or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/selectChargement")
	public String selectChargementAction(Model model) {
		return selectChargement(model);
	}

	/**
	 * Action trigger when the table of load rule is asked or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/addChargement")
	public String addChargementAction(Model model) {
		return addChargement(model);
	}

	/**
	 * Action trigger by adding a load rule in the GUI. Update the GUI and the
	 * database
	 *
	 * @return
	 */
	@RequestMapping("/secure/deleteChargement")
	public String deleteChargementAction(Model model) {
		return deleteChargement(model);
	}

	/**
	 * Action trigger by updating a load rule in the GUI. Update the GUI and the
	 * database.
	 * 
	 * @return
	 */
	@RequestMapping("/secure/updateChargement")
	public String updateChargementAction(Model model) {
		return updateChargement(model);
	}

	/**
	 * Action trigger by updating a load rule in the GUI. Update the GUI and the
	 * database.
	 * 
	 * @return
	 */
	@RequestMapping("/secure/sortChargement")
	public String sortChargementAction(Model model) {
		return sortChargement(model);
	}

	/**
	 * Action trigger by importing load rules in the GUI. Update the GUI and the
	 * database.
	 * 
	 * @return
	 */
	@RequestMapping("/secure/importChargement")
	public String importChargementAction(Model model, MultipartFile fileUploadLoad) {
		return importChargement(model, fileUploadLoad);
	}

	/**
	 * Clean the loading rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/secure/viderChargement")
	public String viderChargementAction(Model model) {
		return viderChargement(model);
	}

}
