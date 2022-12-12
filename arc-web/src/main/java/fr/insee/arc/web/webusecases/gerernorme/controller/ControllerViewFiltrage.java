package fr.insee.arc.web.webusecases.gerernorme.controller;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.web.webusecases.gerernorme.HubControllerGererNorme;

@Component
public class ControllerViewFiltrage extends HubControllerGererNorme {

	/**
	 * Action trigger when the table of map rules is request or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectFiltrage")
	public String selectFiltrageAction(Model model) {
		return selectFiltrage(model);
	}
	
	/**
	 * Action trigger by updating a filter rule in the GUI. Update the GUI and the
	 * database. Before inserting, the rules are checked
	 * 
	 * @return
	 */
	@RequestMapping("/updateFiltrage")
	public String updateFiltrageAction(Model model) {
		return updateFiltrage(model);
	}

	
	/**
	 * Action trigger by deleting a structurize rule in the GUI. Update the GUI and
	 * the database
	 * 
	 * @return
	 */
	@RequestMapping("/deleteFiltrage")
	public String deleteFiltrageAction(Model model) {
		return deleteFiltrage(model);
	}

	/**
	 * Action trigger by sorting the filter rules in the GUI. Update the GUI
	 * 
	 * @return
	 */
	@RequestMapping("/sortFiltrage")
	public String sortFiltrageAction(Model model) {
		return sortFiltrage(model);
	}
	
	/**
	 * Clean the filter rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderFiltrage")
	public String viderFiltrageAction(Model model) {
		return viderFiltrage(model);
	}
	
	/**
	 * Action trigger by uploading a filter rule file
	 * 
	 * @return
	 */
	@RequestMapping("/importFiltrage")
	public String importFiltrageAction(Model model, MultipartFile fileUploadFilter) {
		return importFiltrage(model, fileUploadFilter);
	}


	/**
	 * Action initializing the filter rules
	 * 
	 * @return
	 */
	@RequestMapping("/preGenererRegleFiltrage")
	public String preGenererRegleFiltrageAction(Model model) {
		return preGenererRegleFiltrage(model);
	}
}
