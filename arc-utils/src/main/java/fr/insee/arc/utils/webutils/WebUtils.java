package fr.insee.arc.utils.webutils;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class WebUtils {

	private WebUtils() {
		throw new IllegalStateException("Utility class");
	}

	private static PropertiesHandler properties = PropertiesHandler.getInstance();

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

		String status = UtilitaireDao.isConnectionOk("arc") ? HEALTHCHECK_RESULT_UP : HEALTHCHECK_RESULT_DOWN;

		map.put(HEALTHCHECK_ATTRIBUTE_STATUS, status);

		Map<String, Object> details = new HashMap<>();
		map.put(HEALTHCHECK_ATTRIBUTE_DETAILS, details);

		HashMap<String, String> dbHealthCheck = new HashMap<>();
		details.put(HEALTHCHECK_ATTRIBUTE_DATABASEHEALTHCHECK, dbHealthCheck);
		dbHealthCheck.put(HEALTHCHECK_ATTRIBUTE_STATUS, status);

		map.put(HEALTHCHECK_ATTRIBUTE_VERSION, properties.getVersion());

		return map.getOrDefault(HEALTHCHECK_ATTRIBUTE_STATUS, "").equals(HEALTHCHECK_RESULT_UP);
	}

	public static Map<String, String> fullVersionInformation() {
		return properties.fullVersionInformation();
	}

}
