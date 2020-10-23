package fr.insee.arc.web.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.model.viewobjects.ViewArchiveBAS;
import fr.insee.arc.web.model.viewobjects.ViewEntrepotBAS;
import fr.insee.arc.web.model.viewobjects.ViewFichierBAS;
import fr.insee.arc.web.model.viewobjects.ViewPilotageBAS;
import fr.insee.arc.web.model.viewobjects.ViewRapportBAS;
import fr.insee.arc.web.util.VObject;

@Component
public class EnvManagementModel implements ArcModel {

	private VObject viewPilotageBAS;

	private VObject viewRapportBAS;

	private VObject viewFichierBAS;

	private VObject viewEntrepotBAS;

	private VObject viewArchiveBAS;
	
	public EnvManagementModel() {
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


}