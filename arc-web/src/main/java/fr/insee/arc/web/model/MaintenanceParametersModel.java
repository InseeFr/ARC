package fr.insee.arc.web.model;

import fr.insee.arc.web.model.viewobjects.ViewParameters;
import fr.insee.arc.web.util.VObject;

public class ManageParametersModel implements ArcModel {

    private VObject viewParameters;


    public ManageParametersModel() {
		this.viewParameters = new ViewParameters();
	}

	public VObject getViewParameters() {
		return viewParameters;
	}

	public void setViewParameters(VObject viewParameters) {
		this.viewParameters = viewParameters;
	}

}
