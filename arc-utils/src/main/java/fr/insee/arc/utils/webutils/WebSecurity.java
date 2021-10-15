package fr.insee.arc.utils.webutils;

import org.keycloak.representations.adapters.config.AdapterConfig;

public class WebSecurity {

	
	private WebSecurity() {
	    throw new IllegalStateException("Utility class");
	  }
	
	private static final String ENV_VAR_PREFIX="${";
	private static final String ENV_VAR_SUFFIX="}";	
	
	public static boolean isKeycloackOverloaded(AdapterConfig adapterConfig)
	{
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
	
}
