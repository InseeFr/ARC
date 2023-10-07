package fr.insee.arc.core.service.global.util;


/**
 * Patch methods
 * Often for deprecated
 * @author FY2QEQ
 *
 */
public class Patch {

	  private Patch() {
		    throw new IllegalStateException("Utility class");
		  }

	
	public static String normalizeSchemaName(String envExecution)
	{
		return envExecution.replace(".", "_").toLowerCase();
	}
	
	
}
