package fr.insee.arc.web.gui.famillenorme.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.util.VObject;

@Component
public class ModelGererFamille implements ArcModel {

	private VObject viewFamilleNorme;

	private VObject viewClient;

	private VObject viewTableMetier;

	private VObject viewHostAllowed;

	private VObject viewVariableMetier;

	public ModelGererFamille() {
		this.viewClient = new ViewClient();
		this.viewFamilleNorme = new ViewFamilleNorme();
		this.viewTableMetier = new ViewTableMetier();
		this.viewHostAllowed = new ViewHostAllowed();
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

	public VObject getViewHostAllowed() {
		return viewHostAllowed;
	}

	public void setViewHostAllowed(VObject viewHostAllowed) {
		this.viewHostAllowed = viewHostAllowed;
	}

	public VObject getViewVariableMetier() {
		return viewVariableMetier;
	}

	public void setViewVariableMetier(VObject viewVariableMetier) {
		this.viewVariableMetier = viewVariableMetier;
	}

}