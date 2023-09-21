package fr.insee.arc.core.factory;

import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.p0initialisation.ApiInitialisationService;

public class ApiInitialisationServiceFactory implements IServiceFactory {

	@Override
	public ApiService get(String phaseService, String executionSchema, String directory, Integer capacityParameter, String paramBatch) {
		return new ApiInitialisationService(phaseService, executionSchema, directory, capacityParameter, paramBatch);	
	}

	public static IServiceFactory getInstance() {
		return new ApiInitialisationServiceFactory();
	}

}
