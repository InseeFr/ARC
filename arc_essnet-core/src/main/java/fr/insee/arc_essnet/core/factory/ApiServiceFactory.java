package fr.insee.arc_essnet.core.factory;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import fr.insee.arc_essnet.core.model.TypeTraitementPhase;
import fr.insee.arc_essnet.core.service.AbstractPhaseService;

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
	this.map.put(TypeTraitementPhase.INITIALIZE.toString(), ApiInitialisationServiceFactory.getInstance());
	this.map.put(TypeTraitementPhase.REGISTER.toString(), ApiReceptionServiceFactory.getInstance());
	this.map.put(TypeTraitementPhase.IDENTIFY.toString(), ApiIdentificationServiceFactory.getInstance());
	this.map.put(TypeTraitementPhase.LOAD.toString(), ApiChargementServiceFactory.getInstance());
	this.map.put(TypeTraitementPhase.STRUCTURIZE_XML.toString(), ApiNormageServiceFactory.getInstance());
	this.map.put(TypeTraitementPhase.CONTROL.toString(), ApiControleServiceFactory.getInstance());
	this.map.put(TypeTraitementPhase.FILTER.toString(), ApiFiltrageServiceFactory.getInstance());
	this.map.put(TypeTraitementPhase.FORMAT_TO_MODEL.toString(), ApiMappingServiceFactory.getInstance());
//	this.map.put(TraitementPhase.TRANSFORMATION.toString(), ApiTransformationServiceFactory.getInstance());
	this.map.put(TypeTraitementPhase.DUMMY.toString(), ApiDummyServiceFactory.getInstance());
    }

    private static final ApiServiceFactory getInstance() {
	if (instance == null) {
	    instance = new ApiServiceFactory(new HashMap<String, IServiceFactory>());
	}
	return instance;
    }

    /**
     * Méthode d'entrée unique pour cette factory. Fournir le nom de la phase et les
     * arguments de type {@code String} au constructeur.
     * 
     * @param phaseService
     *            la phase en question
     * @param args
     *            seule la méthode {@link IServiceFactory#get(String...)} connaît le
     *            sens des arguments injectés au constructeur. Seule contrainte : le
     *            premier argument de cette méthode est la phase courante.
     */
    public static final AbstractPhaseService getService(Connection connexion,String phaseService, String... args)
	    throws NullPointerException {
	
	// on repercute la phase dans les parametre à envoyer en premiere
	// position
	String[] args2 = new String[args.length + 1];
	args2[0] = phaseService;
	
	for (int i = 0; i < args.length; i++) {
	    args2[i + 1] = args[i];
	}
	
	if (connexion==null) {
	    return getInstance().map.get(phaseService).get(args2);
	    
	} else {
	    return getInstance().map.get(phaseService).get(connexion, args2);
	}
    }
    
    public static final AbstractPhaseService getService(String phaseService, String... args)
	    throws NullPointerException {

	return getService(null,phaseService,args );
    }

    
}
