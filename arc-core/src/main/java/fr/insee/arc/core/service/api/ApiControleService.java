package fr.insee.arc.core.service.api;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.thread.MultiThreading;
import fr.insee.arc.core.service.thread.ThreadControleService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;


/**
 * ApiChargementService
 *
 * 1- Créer les tables de reception du chargement</br>
 * 2- Récupérer la liste des fichiers à traiter et le nom de leur entrepôt</br>
 * 3- Pour chaque fichier, determiner son format de lecture (zip, tgz, raw) et le chargeur à utlisé (voir entrepot)</br> 
 * 4- Pour chaque fichier, invoquer le chargeur</br> 
 *  4-1 Parsing du fichier</br> 
 *  4-2 Insertion dans les tables I et A des données lues dans le fichier</br> 
 *  4-3 Fin du parsing. Constituer la requete de mise en relation des données chargées et la stocker pour son utilisation ultérieure au normage</br>
 * 5- Fin chargement. Insertion dans la table applicative CHARGEMENT_OK. Mise à jour de la table de pilotage</br>
 *
 * @author Manuel SOULIER
 *
 */
@Component
public class ApiControleService extends ApiService {

    public ApiControleService() {
        super();
    }

    public ApiControleService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
            String paramBatch) {
        super(aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
    }

    /**
     *
     * @param anExecutionEnvironment
     * @param directoryRoot
     * @param aPreviousPhase
     * @param aCurrentPhase
     * @param aNbEnr
     */

    @Override
    public void executer() throws ArcException {

        StaticLoggerDispatcher.info(LOGGER_APISERVICE, "** executer **");
        
		PropertiesHandler properties = PropertiesHandler.getInstance();
        
        BDParameters bdParameters=new BDParameters(ArcDatabase.COORDINATOR);

        this.maxParallelWorkers = bdParameters.getInt(this.connexion.getCoordinatorConnection(), "ApiControleService.MAX_PARALLEL_WORKERS",3);

        this.setTabIdSource(recuperationIdSource());
        
        MultiThreading<ApiControleService,ThreadControleService> mt=new MultiThreading<>(this, new ThreadControleService());
        mt.execute(maxParallelWorkers, getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()), this.envExecution, properties.getDatabaseRestrictedUsername());

    }
    
}
