package fr.insee.arc.web.gui.maintenanceoperation.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.gui.all.util.VObject;

@Component
public class MaintenanceOperationsModel implements ArcModel {

    private VObject viewOperations;
    private VObject viewKube;


    public MaintenanceOperationsModel() {
		this.viewOperations = new ViewOperations();
		this.viewKube = new ViewKube();
	}

	public VObject getViewOperations() {
		return viewOperations;
	}

	public void setViewOperations(VObject viewOperations) {
		this.viewOperations = viewOperations;
	}

	public VObject getViewKube() {
		return viewKube;
	}

	public void setViewKube(VObject viewKube) {
		this.viewKube = viewKube;
	}

}
