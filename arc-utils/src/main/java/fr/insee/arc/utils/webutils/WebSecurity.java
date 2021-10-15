package fr.insee.arc.utils.webutils;

import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.core.io.ClassPathResource;

import fr.insee.arc.utils.utils.LoggerHelper;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebSecurity {
	private static final Logger LOGGER = LogManager.getLogger(WebSecurity.class);

	
	private WebSecurity() {
	    throw new IllegalStateException("Utility class");
	  }
	
	private static final String ENV_VAR_PREFIX="${";
	private static final String ENV_VAR_SUFFIX="}";	
	
	public static boolean isKeycloackOverloaded(AdapterConfig adapterConfig)
	{
        LoggerHelper.infoAsComment(LOGGER, "keycloak adapterConfig.getRealm() : "+adapterConfig.getRealm());
        LoggerHelper.infoAsComment(LOGGER, "keycloak adapterConfig.getAuthServerUrl() : "+adapterConfig.getAuthServerUrl());
		return isOverloaded(adapterConfig.getRealm()) 
				&& isOverloaded(adapterConfig.getAuthServerUrl()) 
				;
	}
	
	/**
	 * check if the value should be overloaded by an environment value 
	 * @param myValue
	 * @return
	 */
	public static boolean isOverloaded(String myValue)
	{
		return !(myValue.startsWith(ENV_VAR_PREFIX) && myValue.endsWith(ENV_VAR_SUFFIX));
	}
	
	

	/**
	 * Keycloak file configuration is active if the property to the file path is not empty
	 * @return
	 */
	public static boolean isKeycloakFileConfigurationActive(String keycloakFile) {
		return !keycloakFile.isEmpty();
	}
	
	/**
	 * Keycloak ressource configuration is active if
	 * 1- the property to the classpath ressource is not empty
	 * 2- the parameters in the classpath had been well overloaded by environment variables
	 * @return
	 */
	public static boolean isKeycloakResourceConfigurationActive(String keycloakResource) {
		// resource empty ? false
		if (keycloakResource.isEmpty()) {return false;}
		
		// ressource well built ? true else false
		try {
			return isKeycloackOverloaded(
			   					 KeycloakDeploymentBuilder.loadAdapterConfig(new ClassPathResource(keycloakResource).getInputStream()));
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Returns true if Keycloak authentification should be used.
	 * Keycloak is active if one of the ressource or file configuration is active
	 * @return
	 */
	public static boolean isKeycloakActive(String keycloakFile, String keycloakResource) {
		return isKeycloakFileConfigurationActive(keycloakFile) || isKeycloakResourceConfigurationActive(keycloakResource);
	}
	
}
