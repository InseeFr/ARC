package fr.insee.arc.web.gui.maintenanceoperation.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.gui.all.util.VObject;

@Component
public class MaintenanceOperationsModel implements ArcModel {

	private String httpOutput;
	
    private VObject viewOperations;
    private VObject viewKubernetes;


    public MaintenanceOperationsModel() {
		this.viewOperations = new ViewOperations();
		this.viewKubernetes = new ViewKubernetes();
	}

	public VObject getViewOperations() {
		return viewOperations;
	}

	public void setViewOperations(VObject viewOperations) {
		this.viewOperations = viewOperations;
	}

	public VObject getViewKubernetes() {
		return viewKubernetes;
	}

	public void setViewKubernetes(VObject viewKubernetes) {
		this.viewKubernetes = viewKubernetes;
	}

	public String getHttpOutput() {
		return httpOutput;
	}

	public void setHttpOutput(String httpOutput) {
		this.httpOutput = httpOutput;
	}
	
	
}
