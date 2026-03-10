package fr.insee.arc.web.gui.report.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.report.service.ServiceViewReport;

@Controller
public class ControllerViewReport extends ServiceViewReport {

	/**
	 * Action trigger by selecting a calendar in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	@RequestMapping("/secure/selectReport")
	public String selectReportAction(Model model) {
		return selectReport(model);
	}

}
