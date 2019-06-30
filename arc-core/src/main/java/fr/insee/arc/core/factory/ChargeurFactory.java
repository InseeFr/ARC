package fr.insee.arc.core.factory;

import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.insee.arc.core.archive_loader.FilesInputStreamLoad;
import fr.insee.arc.core.service.chargeur.ChargeurClefValeur;
import fr.insee.arc.core.service.chargeur.ChargeurXml;
import fr.insee.arc.core.service.chargeur.ILoader;
import fr.insee.arc.core.service.chargeur.LoaderCSV;
import fr.insee.arc.core.service.thread.ThreadLoadService;
import fr.insee.arc.core.util.TypeChargement;
import fr.insee.arc.utils.utils.LoggerDispatcher;


/**
 * Permet de choisir le chargeur que l'on va utiliser
 * @author S4LWO8
 *
 */
public class ChargeurFactory {
    private Map<TypeChargement, ILoader> map = new EnumMap<>(TypeChargement.class);
    private static final Logger LOGGER = Logger.getLogger(ChargeurFactory.class);


    public ChargeurFactory(ThreadLoadService threadChargementService, FilesInputStreamLoad filesInputStreamLoad ) {
        this.map.put(TypeChargement.XML,
                new ChargeurXml(threadChargementService,filesInputStreamLoad));
        this.map.put(TypeChargement.CLEF_VALEUR,
                new ChargeurClefValeur(threadChargementService,filesInputStreamLoad));
        this.map.put(TypeChargement.PLAT,
                new LoaderCSV(threadChargementService,filesInputStreamLoad));

    }
    
    public ILoader getChargeur(TypeChargement typeChargement){
        LoggerDispatcher.info("** getChargeur **", LOGGER);
        return this.map.get(typeChargement);
    }

}