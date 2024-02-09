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

}
