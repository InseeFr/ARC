package fr.insee.arc.core.factory;

import fr.insee.arc.core.service.ApiReceptionService;
import fr.insee.arc.core.service.ApiService;

public class ApiReceptionServiceFactory implements IServiceFactory {

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
			return new ApiReceptionService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiReceptionService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);
		}
	}

	public static IServiceFactory getInstance() {
		return new ApiReceptionServiceFactory();
	}

	
}
