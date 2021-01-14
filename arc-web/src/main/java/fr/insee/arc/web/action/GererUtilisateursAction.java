package fr.insee.arc.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.web.dao.UserManagementDao;
import fr.insee.arc.web.model.UserManagementModel;
import fr.insee.arc.web.util.VObject;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GererUtilisateursAction extends ArcAction<UserManagementModel>{

	private static final Logger LOGGER = LogManager.getLogger(GererUtilisateursAction.class);

	private static final String RESULT_SUCCESS = "/jsp/gererUtilisateurs.jsp";

	@Autowired
	private UserManagementDao userManagementDao;

	private VObject viewListProfils;
	private VObject viewListUtilisateursDuProfil;


	@Override
	protected void putAllVObjects(UserManagementModel arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);

		setViewListProfils(vObjectService.preInitialize(arcModel.getViewUserProfiles()));
		setViewListUtilisateursDuProfil(vObjectService.preInitialize(arcModel.getViewUserList()));

		putVObject(getViewListProfils(), t -> initializeListProfils());
		putVObject(getViewListUtilisateursDuProfil(), t -> initializeListUtilisateursDuProfil(getViewListProfils()));

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);	

	}

	private void initializeListProfils() {
		ArrayList<ArrayList<String>> profiles = new ArrayList<>();
		profiles.add(new ArrayList<>());
		profiles.get(0).add("groupe");
		profiles.get(0).add("lib_groupe");
		profiles.add(new ArrayList<>());
		profiles.get(1).add("text");
		profiles.get(1).add("text");
		profiles.addAll(userManagementDao.getListProfils());
		vObjectService.initializeByList(viewListProfils, profiles, new HashMap<>());
	}

	private void initializeListUtilisateursDuProfil(VObject viewListProfils) {
		Optional<String> selectedProfile = selectedProfile(viewListProfils);
		if (selectedProfile.isEmpty()) {
			vObjectService.destroy(viewListUtilisateursDuProfil);
		} else {
			ArrayList<ArrayList<String>> users = new ArrayList<>();
			users.add(new ArrayList<>());
			users.get(0).add("idep");      
			users.get(0).add("nom_prenom");
			users.get(0).add("groupe");    
			users.get(0).add("lib_groupe");
			users.add(new ArrayList<>());
			users.get(1).add("text");
			users.get(1).add("text");
			users.get(1).add("text");
			users.get(1).add("text");
			users.addAll(userManagementDao.getListUsers(selectedProfile.get()));
			vObjectService.initializeByList(viewListUtilisateursDuProfil, 
					users, new HashMap<>());
		}
	}

	/** Return the selected profile name, or an empty optional if there is none.*/
	private Optional<String> selectedProfile(VObject viewListProfils){
		if (viewListProfils.listContentSelected().isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.ofNullable(viewListProfils.mapContentSelected().get("groupe").get(0));
		}
	}
	@Override
	protected String getActionName() {
		return "userManagement";
	}

	@RequestMapping(value = {"/selectGererUtilisateurs", "/selectUserProfiles"})
	public String selectGererUtilisateurs(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping(value = "/addUserProfiles")
	public String addProfil(Model model) {
		String cn = viewListProfils.getInputFieldFor("groupe");
		if (cn == null) {
			viewListUtilisateursDuProfil.setMessage("userManagement.addProfile.invalid");
		} else if (userManagementDao.addProfile(cn, viewListProfils.getInputFieldFor("lib_groupe"))) {
			viewListUtilisateursDuProfil.setMessage("userManagement.addProfile");
		} else {
			viewListUtilisateursDuProfil.setMessage("userManagement.addProfile.error");
		}
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping(value = "/deleteUserProfiles")
	public String deleteProfil(Model model) {
		ArrayList<String> selectedGroup = viewListProfils.mapContentSelected().get("groupe");
		if (selectedGroup == null || selectedGroup.isEmpty()) {
			viewListUtilisateursDuProfil.setMessage("userManagement.deleteProfile.invalid");
		} else {
			for (String cn : selectedGroup) {
				if (userManagementDao.deleteProfile(cn)) {
					viewListUtilisateursDuProfil.setMessage("userManagement.deleteProfile");
				} else {
					viewListUtilisateursDuProfil.setMessage("userManagement.deleteProfile.error");
					break;
				}
			}
		}
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping(value = "/sortUserProfiles")
	public String sortProfil(Model model) {
		return sortVobject(model, RESULT_SUCCESS, viewListProfils);
	}

	@RequestMapping("/selectUserList")
	public String selectUserList(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping(value = "/addUserList")
	public String addUser(Model model) {
		String userId = viewListUtilisateursDuProfil.getInputFieldFor("idep");
		Optional<String> selectedProfile = selectedProfile(viewListProfils);
		if (selectedProfile.isEmpty()|| userId == null) {
			viewListUtilisateursDuProfil.setMessage("userManagement.addUser.invalid");
		} else if (userManagementDao.addUser(userId, selectedProfile.get())) {
			viewListUtilisateursDuProfil.setMessage("userManagement.addUser");
		} else {
			viewListUtilisateursDuProfil.setMessage("userManagement.addUser.error");
		}
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping(value = "/deleteUserList")
	public String deleteUser(Model model) {
		ArrayList<String> selectedId = viewListUtilisateursDuProfil.mapContentSelected().get("idep");
		Optional<String> selectedProfile = selectedProfile(viewListProfils);
		if (selectedId.isEmpty()|| selectedProfile.isEmpty()) {
			viewListUtilisateursDuProfil.setMessage("userManagement.removeUser.invalid");
		} else {
			for (String userId : selectedId) {
				if (userManagementDao.removeUser(userId, selectedProfile.get())) {
					viewListUtilisateursDuProfil.setMessage("userManagement.removeUser");
				} else {
					viewListUtilisateursDuProfil.setMessage("userManagement.removeUser.error");
					break;
				}
			}
		}
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping(value = "/sortUserList")
	public String sortUser(Model model) {
		return sortVobject(model, RESULT_SUCCESS, viewListUtilisateursDuProfil);
	}

	public VObject getViewListProfils() {
		return viewListProfils;
	}

	public void setViewListProfils(VObject viewListProfils) {
		this.viewListProfils = viewListProfils;
	}

	public VObject getViewListUtilisateursDuProfil() {
		return viewListUtilisateursDuProfil;
	}

	public void setViewListUtilisateursDuProfil(VObject viewListUtilisateursDuProfil) {
		this.viewListUtilisateursDuProfil = viewListUtilisateursDuProfil;
	}

}
