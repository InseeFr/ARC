package fr.insee.arc.web.gui.report.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ServiceViewReport extends InteractorReport {

	/**
	 * Action trigger by selecting a calendar in the GUI. Update the GUI
	 * 
	 * @return success
	 */
	public String selectReport(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String sortReport(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.views.getViewReport());
	}

}
