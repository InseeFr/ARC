package fr.insee.arc_essnet.core.factory;

import java.sql.Connection;

import fr.insee.arc_essnet.core.service.AbstractPhaseService;
import fr.insee.arc_essnet.core.service.ApiIdentifyService;

public class ApiIdentificationServiceFactory implements IServiceFactory {

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
			return new ApiIdentifyService(connexion,args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiIdentifyService(connexion,args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);
		}
	}
	
	@Override
	public AbstractPhaseService get(String... args) {
		if (args.length==5)
		{
			return new ApiIdentifyService(args[0], args[1], args[2], args[3],Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiIdentifyService(args[0], args[1], args[2], args[3],Integer.valueOf(args[4]), args[5]);
		}
	}

	public static IServiceFactory getInstance() {
		return new ApiIdentificationServiceFactory();
	}

}
