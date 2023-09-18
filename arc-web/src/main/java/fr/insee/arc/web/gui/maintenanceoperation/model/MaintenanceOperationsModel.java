package fr.insee.arc.web.gui.maintenanceoperation.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.gui.all.util.VObject;

@Component
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
