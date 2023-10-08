package fr.insee.arc.core.factory;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.p5mapping.ApiMappingService;

public class ApiMappingServiceFactory implements IServiceFactory {

	@Override
	public ApiService get(TraitementPhase phaseService, String executionSchema, String directory, Integer capacityParameter, String paramBatch) {
		return new ApiMappingService(phaseService, executionSchema, directory, capacityParameter, paramBatch);
	}

	public static IServiceFactory getInstance() {
		return new ApiMappingServiceFactory();
	}
}
