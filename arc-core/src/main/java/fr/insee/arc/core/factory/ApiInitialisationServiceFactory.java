package fr.insee.arc.core.factory;

import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.core.service.ApiService;

public class ApiInitialisationServiceFactory implements IServiceFactory {

	@Override
	public ApiService get(String phaseService, String metaDataSchema, String executionSchema, String directory, Integer capacityParameter, String paramBatch) {
		return new ApiInitialisationService(phaseService, metaDataSchema, executionSchema, directory, capacityParameter, paramBatch);	
	}

	public static IServiceFactory getInstance() {
		return new ApiInitialisationServiceFactory();
	}

}
