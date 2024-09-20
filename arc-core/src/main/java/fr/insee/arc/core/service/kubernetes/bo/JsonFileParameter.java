package fr.insee.arc.core.service.kubernetes.bo;

/**
 * class that map the parameter placeholders found in kubernetes the json
 * configuration files located at src/main/resources/kubernetes
 * 
 * @author FY2QEQ
 *
 */
public class JsonFileParameter {

	private JsonFileParameter() {
		throw new IllegalStateException("Utility class");
	}

	public static final String EXECUTOR_LABEL = "{pg-arc-executor-label}";
	public static final String EXECUTOR_NUMBER = "{pg-arc-executor-number}";
	public static final String EXECUTOR_PASSWORD = "{password}";
	public static final String EXECUTOR_DATABASE = "{database}";
	public static final String EXECUTOR_USER = "{user}";
	public static final String EXECUTOR_PORT = "{port}";
	public static final String IMAGE = "{image}";
	public static final String LIMITS_CPU = "{limits_cpu}";
	public static final String LIMITS_MEMORY = "{limits_memory}";
	public static final String LIMITS_EPHEMERAL = "{limits_ephemeral}";
	public static final String REQUESTS_CPU = "{requests_cpu}";
	public static final String REQUESTS_MEMORY = "{requests_memory}";
	public static final String REQUESTS_EPHEMERAL = "{requests_ephemeral}";
	public static final String GENERIC_EPHEMERAL_VOLUME_SIZE = "{generic_ephemeral_volume_size}";


}
