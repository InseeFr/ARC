package fr.insee.arc.web.dao;

import java.util.ArrayList;

public interface UserManagementDao {

	ArrayList<ArrayList<String>> getListProfils();

	ArrayList<ArrayList<String>> getListUsers(String group);

	boolean addProfile(String name, String description);

	boolean deleteProfile(String inputFieldFor);
	
	boolean addUser(String id, String group);

	boolean removeUser(String id, String group);
}
