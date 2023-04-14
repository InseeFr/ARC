package fr.insee.arc.core.factory;

import fr.insee.arc.core.service.api.ApiNormageService;
import fr.insee.arc.core.service.api.ApiService;

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
	public ApiService get(String phaseService, String metaDataSchema, String executionSchema, String directory, Integer capacityParameter, String paramBatch) {
		return new ApiNormageService(phaseService, metaDataSchema, executionSchema, directory, capacityParameter, paramBatch);	
	}

	public static IServiceFactory getInstance() {
		return new ApiNormageServiceFactory();
	}

}
