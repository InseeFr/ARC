package fr.insee.arc.utils.webutils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class WebAttributesName {

	private WebAttributesName() {
		throw new IllegalStateException("Utility class");
	}

	private static PropertiesHandler properties = PropertiesHandler.getInstance();

	public static final String KEYCLOAK_ATTRIBUTE_REALM = "${fr.insee.keycloak.realm}";
	public static final String KEYCLOAK_ATTRIBUTE_SERVER = "${fr.insee.keycloak.server}";
	public static final String KEYCLOAK_ATTRIBUTE_RESOURCE = "${fr.insee.keycloak.resource}";
	public static final String KEYCLOAK_ATTRIBUTE_CREDENTIALS = "${fr.insee.keycloak.credentials.secret}";

	public static boolean isKeycloakActive(String realmProperty)
	{
		return !realmProperty.equals(KEYCLOAK_ATTRIBUTE_REALM);
	}
	
	// json healthcheck attribute
	private static final String HEALTHCHECK_ATTRIBUTE_STATUS = "status";
	private static final String HEALTHCHECK_ATTRIBUTE_DETAILS = "details";
	private static final String HEALTHCHECK_ATTRIBUTE_DATABASEHEALTHCHECK = "dataBaseHealthCheck";
	private static final String HEALTHCHECK_ATTRIBUTE_VERSION = "version";

	// result values
	private static final String HEALTHCHECK_RESULT_UP = "up";
	private static final String HEALTHCHECK_RESULT_DOWN = "down";

	/**
	 * update healthcheck description in map and return true if status is ok
	 * 
	 * @param map
	 * @return
	 */
	public static boolean getHealthCheckStatus(Map<String, Object> map) {

		String status = UtilitaireDao.get(0).isConnectionOk() ? HEALTHCHECK_RESULT_UP : HEALTHCHECK_RESULT_DOWN;

		map.put(HEALTHCHECK_ATTRIBUTE_STATUS, status);

		Map<String, Object> details = new HashMap<>();
		map.put(HEALTHCHECK_ATTRIBUTE_DETAILS, details);

		Map<String, String> dbHealthCheck = new HashMap<>();
		details.put(HEALTHCHECK_ATTRIBUTE_DATABASEHEALTHCHECK, dbHealthCheck);
		dbHealthCheck.put(HEALTHCHECK_ATTRIBUTE_STATUS, status);

		map.put(HEALTHCHECK_ATTRIBUTE_VERSION, properties.getVersion());

		return map.getOrDefault(HEALTHCHECK_ATTRIBUTE_STATUS, "").equals(HEALTHCHECK_RESULT_UP);
	}

	public static Map<String, String> fullVersionInformation() {
		return properties.fullVersionInformation();
	}

}
