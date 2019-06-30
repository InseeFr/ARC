package fr.insee.arc.core.factory;

import java.sql.Connection;

import fr.insee.arc.core.service.AbstractPhaseService;
import fr.insee.arc.core.service.ApiFiltrageService;

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
	public AbstractPhaseService get(Connection connexion, String... args) {
		if (args.length==5)
		{
			return new ApiFiltrageService(connexion,args[0], args[1], args[2], args[3], Integer.valueOf(args[4]));
		}
		else
		{
			return new ApiFiltrageService(connexion,args[0], args[1], args[2], args[3], Integer.valueOf(args[4]), args[5]);
		}
	}
	public AbstractPhaseService get(String... args) {
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
