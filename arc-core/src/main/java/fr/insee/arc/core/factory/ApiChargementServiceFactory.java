package fr.insee.arc.core.factory;

import java.sql.Connection;

import fr.insee.arc.core.service.AbstractPhaseService;
import fr.insee.arc.core.service.ApiLoadService;
import fr.insee.arc.core.service.ApiReceptionService;

public class ApiChargementServiceFactory implements IServiceFactory {

	public AbstractPhaseService get(Connection connexion, String... args) {
		if (args.length==5)
		{
			return new ApiReceptionService(connexion,args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiReceptionService(connexion,args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);
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
			return new ApiLoadService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiLoadService(args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);
		}	
	}
	
	public static IServiceFactory getInstance() {
		return new ApiChargementServiceFactory();
	}

}
