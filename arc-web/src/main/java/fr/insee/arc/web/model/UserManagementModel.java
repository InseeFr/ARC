package fr.insee.arc.web.model;

import fr.insee.arc.web.model.viewobjects.ViewProfil;
import fr.insee.arc.web.model.viewobjects.ViewUtilisateur;
import fr.insee.arc.web.util.VObject;

public class UserManagementModel  implements ArcModel {

	private VObject viewUserProfiles;
	private VObject viewUserList;
	
	public UserManagementModel() {
		this.viewUserProfiles = new ViewProfil();
		this.viewUserList = new ViewUtilisateur();
	}
	
	public VObject getViewUserProfiles() {
		return viewUserProfiles;
	}
	public void setViewUserProfiles(VObject viewUserProfiles) {
		this.viewUserProfiles = viewUserProfiles;
	}
	public VObject getViewUserList() {
		return viewUserList;
	}
	public void setViewUserList(VObject viewListUtilisateursDuProfil) {
		this.viewUserList = viewListUtilisateursDuProfil;
	}
	
	
	
}
