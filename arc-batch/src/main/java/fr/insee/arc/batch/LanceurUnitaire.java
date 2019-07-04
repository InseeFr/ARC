package fr.insee.arc.batch;

import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;


public class LanceurUnitaire {

	/**
	 * 
	 * @param args
	 *            {@code args[0]} : service à invoquer<br/>
	 *            {@code args[1]} : amount of files to be processed<br/>
	 */
	
	public static void main(String[] args) {
		
	    PropertiesHandler properties =new PropertiesHandler();
		String nb=args[1];
		
		ApiServiceFactory.getService(args[0]
				, properties.getBatchArcEnvironment()
				, properties.getBatchExecutionEnvironment()
				, properties.getBatchParametersDirectory()
				, nb).invokeApi();
	}

}
