package fr.insee.arc.core.service.api;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.service.api.query.ServiceTableNaming;
import fr.insee.arc.core.service.thread.MultiThreading;
import fr.insee.arc.core.service.thread.ThreadNormageService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;


/**
 * ApiNormageService
 *
 * 1- créer la table des données à traiter dans le module</br>
 * 2- calcul de la norme, validité, periodicité sur chaque ligne de la table de donnée</br> 
 * 3- déterminer pour chaque fichier si le normage s'est bien déroulé et marquer sa norme, sa validité et sa périodicité</br> 
 * 4- créer les tables OK et KO; marquer les info de normage(norme, validité, périodicité) sur chaque ligne de donnée</br> 
 * 5- transformation de table de donnée; mise à plat du fichier; suppression et relation</br>
 * 6- mettre à jour le nombre d'enregistrement par fichier après sa transformation</br>
 * 7- sortir les données du module vers l'application</br>
 *
 * @author Manuel SOULIER
 *
 */
@Component
public class ApiNormageService extends ApiService {
    
    public ApiNormageService() {
        super();
    }
    
    public ApiNormageService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
            String paramBatch) {
        super(aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
        this.setTableNorme(ServiceTableNaming.dbEnv(this.getEnvExecution()) + TraitementTableParametre.NORME);
    }

    @Override
    public void executer() throws ArcException {
        StaticLoggerDispatcher.info("** executer **", LOGGER_APISERVICE);
        
        BDParameters bdParameters=new BDParameters(ArcDatabase.COORDINATOR);

        this.maxParallelWorkers = bdParameters.getInt(this.connexion.getCoordinatorConnection(), "ApiNormageService.MAX_PARALLEL_WORKERS",4);

        // récupère le nombre de fichier à traiter
        this.setTabIdSource(recuperationIdSource());
        
        MultiThreading<ApiNormageService,ThreadNormageService> mt=new MultiThreading<>(this, new ThreadNormageService());
        mt.execute(maxParallelWorkers, getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()), this.envExecution, properties.getDatabaseRestrictedUsername());

    }

}
