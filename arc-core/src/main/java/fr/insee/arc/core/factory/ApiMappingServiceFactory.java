package fr.insee.arc.core.factory;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.mutiphase.ApiMultiphaseService;

public class ApiMappingServiceFactory implements IServiceFactory {

	@Override
	public ApiService get(String executionSchema, Integer capacityParameter, String paramBatch) {
		return new ApiMultiphaseService(executionSchema, capacityParameter, paramBatch, TraitementPhase.MAPPING);
	}

	public static IServiceFactory getInstance() {
		return new ApiMappingServiceFactory();
	}
}
