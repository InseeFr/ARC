package fr.insee.arc.core.factory;

import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.core.service.ApiService;

public class ApiInitialisationServiceFactory implements IServiceFactory {

	@Override
	/**
	 *
	 * @param aCurrentPhase
	 * @param anParametersEnvironment
	 * @param aEnvExecution
	 * @param aDirectoryRoot
	 * @param aNbEnr
	 */
	public ApiService get(String... args) {
		if (args.length==5)
		{
			return new ApiInitialisationService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));	
		}
		else
		{
			return new ApiInitialisationService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);
		}
	}

	public static IServiceFactory getInstance() {
		return new ApiInitialisationServiceFactory();
	}

}
