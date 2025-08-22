package fr.insee.arc.web.gui.export.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewExportOption extends InteractorExport {
	public String selectExportOption(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String addExportOption(Model model) {
		this.vObjectService.insert(views.getViewExportOption());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String deleteExportOption(Model model) {
		this.vObjectService.delete(views.getViewExportOption());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String updateExportOption(Model model) {
		this.vObjectService.update(views.getViewExportOption());
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String sortExportOption(Model model) {
		this.vObjectService.sort(views.getViewExportOption());
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
