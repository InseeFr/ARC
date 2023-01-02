package fr.insee.arc.web.gui.pilotage.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.util.VObject;

@Component
public class ModelPilotage implements ArcModel {

	private VObject viewPilotageBAS;

	private VObject viewRapportBAS;

	private VObject viewFichierBAS;

	private VObject viewEntrepotBAS;

	private VObject viewArchiveBAS;
	
	private String entrepotLecture;
	
	private String entrepotEcriture;
	
	public ModelPilotage() {
		this.viewArchiveBAS = new ViewArchiveBAS();
		this.viewEntrepotBAS = new ViewEntrepotBAS();
		this.viewFichierBAS = new ViewFichierBAS();
		this.viewPilotageBAS = new ViewPilotageBAS();
		this.viewRapportBAS = new ViewRapportBAS();
	}
	
	public VObject getViewPilotageBAS() {
		return viewPilotageBAS;
	}

	public VObject getViewRapportBAS() {
		return viewRapportBAS;
	}

	public VObject getViewFichierBAS() {
		return viewFichierBAS;
	}

	public VObject getViewEntrepotBAS() {
		return viewEntrepotBAS;
	}

	public VObject getViewArchiveBAS() {
		return viewArchiveBAS;
	}

	public void setViewPilotageBAS(VObject viewPilotageBAS) {
		this.viewPilotageBAS = viewPilotageBAS;
	}

	public void setViewRapportBAS(VObject viewRapportBAS) {
		this.viewRapportBAS = viewRapportBAS;
	}

	public void setViewFichierBAS(VObject viewFichierBAS) {
		this.viewFichierBAS = viewFichierBAS;
	}

	public void setViewEntrepotBAS(VObject viewEntrepotBAS) {
		this.viewEntrepotBAS = viewEntrepotBAS;
	}

	public void setViewArchiveBAS(VObject viewArchiveBAS) {
		this.viewArchiveBAS = viewArchiveBAS;
	}

	public String getEntrepotLecture() {
		return entrepotLecture;
	}

	public void setEntrepotLecture(String entrepotLecture) {
		this.entrepotLecture = entrepotLecture;
	}

	public String getEntrepotEcriture() {
		return entrepotEcriture;
	}

	public void setEntrepotEcriture(String entrepotEcriture) {
		this.entrepotEcriture = entrepotEcriture;
	}



}