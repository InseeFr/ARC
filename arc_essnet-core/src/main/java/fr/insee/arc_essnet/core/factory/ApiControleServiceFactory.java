package fr.insee.arc_essnet.core.factory;

import java.sql.Connection;

import fr.insee.arc_essnet.core.service.AbstractPhaseService;
import fr.insee.arc_essnet.core.service.ApiControleService;

public class ApiControleServiceFactory implements IServiceFactory {

	public AbstractPhaseService get(Connection connexion, String... args) {
		if (args.length==5)
		{
			return new ApiControleService(connexion,args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiControleService(connexion,args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);
		}
	}
	@Override
	/**
	 *
	 * @param aCurrentPhase
	 * @param anParametersEnvironment
	 * @param aEnvExecution
	 * @param aDirectoryRoot
	 * @param aNbEnr
	 */
	public AbstractPhaseService get(String... args) {
		if (args.length==5)
		{
			return new ApiControleService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiControleService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);
		}
	}

	public static IServiceFactory getInstance() {
		return new ApiControleServiceFactory();
	}

}
