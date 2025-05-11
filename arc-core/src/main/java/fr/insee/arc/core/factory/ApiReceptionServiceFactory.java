package fr.insee.arc.core.factory;

import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.p1reception.ApiReceptionService;

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
	public ApiService get(String executionSchema, Integer capacityParameter, String paramBatch) {
		return new ApiReceptionService(executionSchema, capacityParameter, paramBatch);
	}

	public static IServiceFactory getInstance() {
		return new ApiReceptionServiceFactory();
	}

	
}
