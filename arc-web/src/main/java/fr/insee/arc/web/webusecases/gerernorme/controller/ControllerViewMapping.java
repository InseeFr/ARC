package fr.insee.arc.web.webusecases.gerernorme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.web.webusecases.gerernorme.service.ServiceViewMapping;

@Controller
public class ControllerViewMapping extends ServiceViewMapping {
	

	
	/**
	 * Action trigger when the table of map rules is request or refresh. Update the
	 * GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectMapping")
	public String selectMappingAction(Model model) {
		return selectMapping(model);
	}
	
	
	/**
	 * Action trigger by adding a map rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return
	 */
	@RequestMapping("/addMapping")
	public String addMappingAction(Model model) {
		return addMapping(model);
	}

	/**
	 * Action trigger by delete a map rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return
	 */
	@RequestMapping("/deleteMapping")
	public String deleteMappingAction(Model model) {
		return deleteMapping(model);
	}

	/**
	 * Action trigger by updating a map rule in the GUI. Update the GUI and the
	 * database. Before insertion check if the rule is OK
	 * 
	 * @return
	 */
	@RequestMapping("/updateMapping")
	public String updateMappingAction(Model model) {
		return updateMapping(model);
	}

	/**
	 * Action trigger by sorting the map rules in the GUI. Update the GUI
	 * 
	 * @return
	 */
	@RequestMapping("/sortMapping")
	public String sortMappingAction(Model model) {
		return sortMapping(model);
	}
	
	/**
	 * Clean the map rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderMapping")
	public String viderMappingAction(Model model) {
		return viderMapping(model);
	}
	
	/**
	 * Action trigger by importing a map rule file
	 * 
	 * @return
	 */
	@RequestMapping("/importMapping")
	public String importMappingAction(Model model, MultipartFile fileUploadMap) {
		return importMapping(model, fileUploadMap);
	}

	/**
	 * Action trigger by request the generation of the mapping rule. Will create in
	 * database empty rules for each column in the final model and update the GUI.
	 * 
	 * @return
	 */
	@RequestMapping("/preGenererRegleMapping")
	public String preGenererRegleMappingAction(Model model) {
		return preGenererRegleMapping(model);
	}


}
