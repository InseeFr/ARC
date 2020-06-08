package fr.insee.arc.core.factory;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.ApiService;

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
		this.map.put(TraitementPhase.FILTRAGE.toString(),
				ApiFiltrageServiceFactory.getInstance());
		this.map.put(TraitementPhase.MAPPING.toString(),
				ApiMappingServiceFactory.getInstance());
	}

	private static final ApiServiceFactory getInstance() {
		if (instance == null) {
			instance = new ApiServiceFactory(
					new HashMap<String, IServiceFactory>());
		}
		return instance;
	}

	/**
	 * Méthode d'entrée unique pour cette factory. Fournir le nom de la phase et
	 * les arguments de type {@code String} au constructeur.
	 * 
	 * @param phaseService
	 *            la phase en question
	 * @param args
	 *            seule la méthode {@link IServiceFactory#get(String...)}
	 *            connaît le sens des arguments injectés au constructeur. Seule
	 *            contrainte : le premier argument de cette méthode est la phase
	 *            courante.
	 */
	public static final ApiService getService(String phaseService,
			String... args) {

		// on repercute la phase dans les parametre à envoyer en premiere
		// position
		String[] args2 = new String[args.length + 1];
		args2[0] = phaseService;

		for (int i = 0; i < args.length; i++) {
			args2[i + 1] = args[i];
		}

		return getInstance().map.get(phaseService).get(args2);
	}

}
