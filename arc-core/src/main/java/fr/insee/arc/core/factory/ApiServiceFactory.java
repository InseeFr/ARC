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


	private Map<TraitementPhase, IServiceFactory> map;

	private static ApiServiceFactory instance = null;

	private ApiServiceFactory(Map<TraitementPhase, IServiceFactory> aMap) {
		this.map = aMap;
		this.map.put(TraitementPhase.INITIALISATION,
				ApiInitialisationServiceFactory.getInstance());
		this.map.put(TraitementPhase.RECEPTION,
				ApiReceptionServiceFactory.getInstance());
		this.map.put(TraitementPhase.CHARGEMENT,
				ApiChargementServiceFactory.getInstance());
		this.map.put(TraitementPhase.NORMAGE,
				ApiNormageServiceFactory.getInstance());
		this.map.put(TraitementPhase.CONTROLE,
				ApiControleServiceFactory.getInstance());
		this.map.put(TraitementPhase.MAPPING,
				ApiMappingServiceFactory.getInstance());
		this.map.put(TraitementPhase.EXPORT,
				ApiExportServiceFactory.getInstance());
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
	public static final ApiService getService(TraitementPhase phaseService, String executionSchema, Integer capacityParameter, String paramBatch) {
		return getInstance().map.get(phaseService).get(phaseService, executionSchema, capacityParameter, paramBatch);
	}
}
