package fr.insee.arc.web.model;

import fr.insee.arc.web.model.viewobjects.ViewClient;
import fr.insee.arc.web.model.viewobjects.ViewFamilleNorme;
import fr.insee.arc.web.model.viewobjects.ViewTableMetier;
import fr.insee.arc.web.model.viewobjects.ViewVariableMetier;
import fr.insee.arc.web.util.VObject;

public class FamilyManagementModel implements ArcModel {
	
	private VObject viewFamilleNorme;

    private VObject viewClient;

    private VObject viewTableMetier;

    private VObject  viewVariableMetier;

    public FamilyManagementModel() {
		this.viewClient = new ViewClient();
		this.viewFamilleNorme = new ViewFamilleNorme();
		this.viewTableMetier = new ViewTableMetier();
		this.viewVariableMetier = new ViewVariableMetier();
	}

	public VObject getViewFamilleNorme() {
		return viewFamilleNorme;
	}

	public void setViewFamilleNorme(VObject viewFamilleNorme) {
		this.viewFamilleNorme = viewFamilleNorme;
	}

	public VObject getViewClient() {
		return viewClient;
	}

	public void setViewClient(VObject viewClient) {
		this.viewClient = viewClient;
	}

	public VObject getViewTableMetier() {
		return viewTableMetier;
	}

	public void setViewTableMetier(VObject viewTableMetier) {
		this.viewTableMetier = viewTableMetier;
	}

	public VObject getViewVariableMetier() {
		return viewVariableMetier;
	}

	public void setViewVariableMetier(VObject viewVariableMetier) {
		this.viewVariableMetier = viewVariableMetier;
	}

}