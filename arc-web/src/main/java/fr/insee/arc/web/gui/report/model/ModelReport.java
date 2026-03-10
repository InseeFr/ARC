package fr.insee.arc.web.gui.report.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.gui.all.util.VObject;

@Component
public class ModelReport implements ArcModel {

	// The report view
	private VObject viewReport;


	public ModelReport() {
		this.viewReport = new ViewReport();
	}


	public VObject getViewReport() {
		return viewReport;
	}


	public void setViewReport(VObject viewReport) {
		this.viewReport = viewReport;
	}
}