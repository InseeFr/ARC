package fr.insee.arc.web.gui.famillenorme.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.web.gui.famillenorme.service.ServiceViewFamilleNorme;

@Controller
public class ControllerViewFamilleNorme extends ServiceViewFamilleNorme {
	
	@RequestMapping("/secure/selectFamilleNorme")
	public String selectFamilleNormeAction(Model model) {
		return selectFamilleNorme(model);
	}

	@RequestMapping("/secure/addFamilleNorme")
	public String addFamilleNormeAction(Model model) {
		return addFamilleNorme(model);
	}

	@RequestMapping("/secure/deleteFamilleNorme")
	public String deleteFamilleNormeAction(Model model) {
		return deleteFamilleNorme(model);
	}

	@RequestMapping("/secure/updateFamilleNorme")
	public String updateFamilleNormeAction(Model model) {
		return updateFamilleNorme(model);
	}

	@RequestMapping("/secure/sortFamilleNorme")
	public String sortFamilleNormeAction(Model model) {
		return sortFamilleNorme(model);
	}

	@RequestMapping("/secure/downloadFamilleNorme")
	public String downloadFamilleNormeAction(Model model, HttpServletResponse response) {
		return downloadFamilleNorme(model, response);
	}
	
	@RequestMapping("/secure/importFamilleNorme")
	public String uploadFamilleNormeAction(Model model, MultipartFile fileUpload) {
		return uploadFamilleNorme(model, fileUpload);
	}

	@RequestMapping("/secure/importDDI")
	public String importDDIAction(Model model, MultipartFile fileUpload) {
		return importDDI(model, fileUpload);
	}

}
