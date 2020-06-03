package fr.insee.arc.core.factory;

import fr.insee.arc.core.service.ApiFiltrageService;
import fr.insee.arc.core.service.ApiService;

public class ApiFiltrageServiceFactory implements IServiceFactory {

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
			return new ApiFiltrageService(args[0], args[1], args[2], args[3],Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiFiltrageService(args[0], args[1], args[2], args[3],Integer.valueOf(args[4]), args[5]);
		}
	}

	public static IServiceFactory getInstance() {
		return new ApiFiltrageServiceFactory();
	}

}
