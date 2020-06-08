package fr.insee.arc.core.factory;

import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.service.ApiMappingService;

public class ApiMappingServiceFactory implements IServiceFactory {

	/**
	 * 
	 * @param aCurrentPhase
	 * @param anParametersEnvironment
	 * @param aEnvExecution
	 * @param aDirectoryRoot
	 * @param aNbEnr
	 */
	@Override
	public ApiService get(String... args) {
		if (args.length==5)
		{
			return new ApiMappingService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiMappingService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);
		}
	}

	public static IServiceFactory getInstance() {
		return new ApiMappingServiceFactory();
	}
}
