package fr.insee.arc.web.gui.maintenanceparametre.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.gui.all.util.VObject;

@Component
public class ModelMaintenanceParametre implements ArcModel {

    private VObject viewParameters;


    public ModelMaintenanceParametre() {
		this.viewParameters = new ViewParameters();
	}

	public VObject getViewParameters() {
		return viewParameters;
	}

	public void setViewParameters(VObject viewParameters) {
		this.viewParameters = viewParameters;
	}

}
