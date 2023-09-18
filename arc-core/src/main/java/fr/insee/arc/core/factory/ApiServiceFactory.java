package fr.insee.arc.core.factory;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;

/**
 * 
 * @author QV47IK
 * 
 */
public class ApiServiceFactory {


	private Map<String, IServiceFactory> map;

	private static ApiServiceFactory instance = null;

	private ApiServiceFactory(Map<String, IServiceFactory> aMap) {
		this.map = aMap;
		this.map.put(TraitementPhase.INITIALISATION.toString(),
				ApiInitialisationServiceFactory.getInstance());
		this.map.put(TraitementPhase.RECEPTION.toString(),
				ApiReceptionServiceFactory.getInstance());
		this.map.put(TraitementPhase.CHARGEMENT.toString(),
				ApiChargementServiceFactory.getInstance());
		this.map.put(TraitementPhase.NORMAGE.toString(),
				ApiNormageServiceFactory.getInstance());
		this.map.put(TraitementPhase.CONTROLE.toString(),
				ApiControleServiceFactory.getInstance());
		this.map.put(TraitementPhase.MAPPING.toString(),
				ApiMappingServiceFactory.getInstance());
	}

	private static final ApiServiceFactory getInstance() {
		if (instance == null) {
			instance = new ApiServiceFactory(
					new HashMap<>());
		}
		return instance;
	}

	/**
	 * instanciate an arc service with the parameters
	 * @param phaseService
	 * @param metaDataSchema
	 * @param executionSchema
	 * @param directory
	 * @param capacityParameter
	 * @param paramBatch
	 * @return
	 */
	public static final ApiService getService(String phaseService, String metaDataSchema, String executionSchema, String directory, Integer capacityParameter, String paramBatch) {
		return getInstance().map.get(phaseService).get(phaseService, metaDataSchema, executionSchema, directory, capacityParameter, paramBatch);
	}
}
