package fr.insee.arc.core.factory;

import fr.insee.arc.core.service.global.ApiService;

public interface IServiceFactory {
	
	/**
	 * 
	 * @param phaseService : the name of the phase to invoke
	 * @param metaDataSchema : the name of the metadata schema containing rules
	 * @param executionSchema : the name of the execution schema
	 * @param directory
	 * @param capacityParameter
	 * @param paramBatch
	 * @return
	 */
	public ApiService get(String phaseService, String metaDataSchema, String executionSchema, String directory, Integer capacityParameter, String paramBatch);

}
