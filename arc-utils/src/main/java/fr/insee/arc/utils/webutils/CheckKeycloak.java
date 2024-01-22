package fr.insee.arc.utils.webutils;

import org.springframework.beans.factory.annotation.Value;

public class CheckKeycloak {

	@Value("${fr.insee.keycloak.realm}")
	private String keycloakRealm;
	
	public boolean isKeycloakActive()
	{
		return keycloakRealm!=null;
	}
	
	protected void setKeycloak(String keycloakRealm) {
		this.keycloakRealm = keycloakRealm;
	}
	
}
