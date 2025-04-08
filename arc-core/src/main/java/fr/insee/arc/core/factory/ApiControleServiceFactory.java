package fr.insee.arc.core.factory;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.mutiphase.ApiMultiphaseService;
import fr.insee.arc.core.service.p4controle.ApiControleService;

public class ApiControleServiceFactory implements IServiceFactory {

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
		return new ApiMultiphaseService(executionSchema, capacityParameter, paramBatch, TraitementPhase.CONTROLE);
	}

	public static IServiceFactory getInstance() {
		return new ApiControleServiceFactory();
	}

}
