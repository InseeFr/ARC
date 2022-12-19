package fr.insee.arc.web.gui.norme.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.web.gui.norme.service.ServiceViewControle;

@Controller
public class ControllerViewControle extends ServiceViewControle {

	/**
	 * Action trigger when the table of control rules is request or refresh. Update
	 * the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/selectControle")
	public String selectControleAction(Model model) {
		return selectControle(model);
	}

	/**
	 * Action trigger by adding a control rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return success
	 */
	@RequestMapping("/addControle")
	public String addControleAction(Model model) {
		return addControle(model);
	}

	/**
	 * Action trigger by deleting a control rule in the GUI. Update the GUI and the
	 * database
	 * 
	 * @return
	 */
	@RequestMapping("/deleteControle")
	public String deleteControleAction(Model model) {
		return deleteControle(model);
	}

	/**
	 * Action trigger by updating some control rules in the GUI. Update the GUI and
	 * the database. Before insertion in data base check if the news rules are
	 * coherents
	 * 
	 * @return
	 */
	@RequestMapping("/updateControle")
	public String updateControleAction(Model model) {
		return updateControle(model);
	}

	/**
	 * Action trigger by sorting a control rules in the GUI. Update the GUI .
	 * 
	 * @return
	 */
	@RequestMapping("/sortControle")
	public String sortControleAction(Model model) {
		return sortControle(model);
	}

	/**
	 * Action trigger by uploading a file with rule
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/importControle")
	public String importControleAction(Model model, MultipartFile fileUploadControle) {
		return importControle(model, fileUploadControle);
	}

	/**
	 * Clean the control rules. Update GUI and database
	 * 
	 * @return
	 */
	@RequestMapping("/viderControle")
	public String viderControleAction(Model model) {
		return viderControle(model);
	}

}
