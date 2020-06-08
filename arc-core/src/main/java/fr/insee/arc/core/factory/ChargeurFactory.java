package fr.insee.arc.core.factory;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.insee.arc.core.service.ApiChargementService;
import fr.insee.arc.core.service.engine.chargeur.ChargeurCSV;
import fr.insee.arc.core.service.engine.chargeur.ChargeurClefValeur;
import fr.insee.arc.core.service.engine.chargeur.ChargeurXml;
import fr.insee.arc.core.service.engine.chargeur.ChargeurXmlComplexe;
import fr.insee.arc.core.service.engine.chargeur.IChargeur;
import fr.insee.arc.core.service.thread.ThreadChargementService;
import fr.insee.arc.core.util.TypeChargement;
import fr.insee.arc.utils.utils.LoggerDispatcher;


/**
 * Permet de choisir le chargeur que l'on va utiliser
 * @author S4LWO8
 *
 */
public class ChargeurFactory {
    private Map<TypeChargement, IChargeur> map = new HashMap<TypeChargement, IChargeur>();
    private static final Logger LOGGER = Logger.getLogger(ChargeurFactory.class);


    public ChargeurFactory(ThreadChargementService threadChargementService, String fileName) {
        this.map.put(TypeChargement.XML,
                new ChargeurXml(threadChargementService,fileName));
        this.map.put(TypeChargement.XML_COMPLEXE,
                new ChargeurXmlComplexe(threadChargementService,fileName));
        this.map.put(TypeChargement.CLEF_VALEUR,
                new ChargeurClefValeur(threadChargementService,fileName));
        this.map.put(TypeChargement.PLAT,
                new ChargeurCSV(threadChargementService,fileName));

    }
    
    public IChargeur getChargeur(TypeChargement typeChargement){
        LoggerDispatcher.info("** getChargeur **", LOGGER);
        return this.map.get(typeChargement);
    }

}