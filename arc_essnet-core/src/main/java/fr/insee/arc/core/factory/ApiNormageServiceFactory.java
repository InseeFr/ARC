package fr.insee.arc.core.factory;

import java.sql.Connection;

import fr.insee.arc.core.service.AbstractPhaseService;
import fr.insee.arc.core.service.ApiNormageService;

public class ApiNormageServiceFactory implements IServiceFactory {

	@Override
	/**
	 *
	 * @param aCurrentPhase
	 * @param anParametersEnvironment
	 * @param aEnvExecution
	 * @param aDirectoryRoot
	 * @param aNbEnr
	 */
	public AbstractPhaseService get(Connection connexion, String... args) {
		if (args.length==5)
		{
			return new ApiNormageService(connexion,args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiNormageService(connexion,args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);
		}
	}
	
	public AbstractPhaseService get(String... args) {
		if (args.length==5)
		{
			return new ApiNormageService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiNormageService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);	
		}
	}

	public static IServiceFactory getInstance() {
		return new ApiNormageServiceFactory();
	}

}
