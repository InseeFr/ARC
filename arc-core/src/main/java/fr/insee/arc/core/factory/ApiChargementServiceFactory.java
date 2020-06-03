package fr.insee.arc.core.factory;

import fr.insee.arc.core.service.ApiChargementService;
import fr.insee.arc.core.service.ApiService;

public class ApiChargementServiceFactory implements IServiceFactory {

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
			return new ApiChargementService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiChargementService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);
		}	
	}
	
	public static IServiceFactory getInstance() {
		return new ApiChargementServiceFactory();
	}

}
