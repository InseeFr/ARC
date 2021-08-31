package fr.insee.arc.web.model;

import fr.insee.arc.web.model.viewobjects.ViewOperations;
import fr.insee.arc.web.util.VObject;

public class MaintenanceOperationsModel implements ArcModel {

    private VObject viewOperations;


    public MaintenanceOperationsModel() {
		this.viewOperations = new ViewOperations();
	}

	public VObject getViewOperations() {
		return viewOperations;
	}

	public void setViewOperations(VObject viewOperations) {
		this.viewOperations = viewOperations;
	}

}
