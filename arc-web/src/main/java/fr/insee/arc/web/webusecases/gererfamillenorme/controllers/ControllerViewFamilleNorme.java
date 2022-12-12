package fr.insee.arc.web.webusecases.gererfamillenorme.controllers;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.web.webusecases.gererfamillenorme.services.ServiceViewFamilleNorme;

@Component
public class ControllerViewFamilleNorme extends ServiceViewFamilleNorme {
	
	@RequestMapping("/selectFamilleNorme")
	public String selectFamilleNormeAction(Model model) {
		return selectFamilleNorme(model);
	}

	@RequestMapping("/addFamilleNorme")
	public String addFamilleNormeAction(Model model) {
		return addFamilleNorme(model);
	}

	@RequestMapping("/deleteFamilleNorme")
	public String deleteFamilleNormeAction(Model model) {
		return deleteFamilleNorme(model);
	}

	@RequestMapping("/updateFamilleNorme")
	public String updateFamilleNormeAction(Model model) {
		return updateFamilleNorme(model);
	}

	@RequestMapping("/sortFamilleNorme")
	public String sortFamilleNormeAction(Model model) {
		return sortFamilleNorme(model);
	}

	@RequestMapping("/downloadFamilleNorme")
	public String downloadFamilleNormeAction(Model model, HttpServletResponse response) {
		return downloadFamilleNorme(model, response);
	}

	@RequestMapping("/importDDI")
	public String importDDIAction(Model model, MultipartFile fileUploadDDI) {
		return importDDI(model, fileUploadDDI);
	}

}
