package fr.insee.arc.core.service.api;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.api.query.ServiceFileSystemManagement;
import fr.insee.arc.core.service.thread.MultiThreading;
import fr.insee.arc.core.service.thread.ThreadChargementService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.Norme;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;


/**
 * ApiChargementService
 *
 * 1- Créer les tables de reception du chargement</br> 2- Récupérer la liste des fichiers à traiter et le nom de leur entrepôt 3- Pour
 * chaque fichier, determiner son format de lecture (zip, tgz, raw) et le chargeur à utlisé (voir entrepot) 4- Pour chaque fichier, Invoquer
 * le chargeur 4-1 Parsing du fichier 4-2 Insertion dans les tables I et A des données lues dans le fichier 4-3 Fin du parsing. Constituer
 * la requete de mise en relation des données chargées et la stocker pour son utilisation ultérieure au normage 5- Fin chargement. Insertion
 * dans la table applicative CHARGEMENT_OK. Mise à jour de la table de pilotage
 *
 * @author Manuel SOULIER
 *
 */



@Component
public class ApiChargementService extends ApiService {
    private static final Logger LOGGER = LogManager.getLogger(ApiChargementService.class);
    
    public ApiChargementService() {
        super();
    }

    public ApiChargementService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
            String paramBatch) {
        super(aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
    }


    @Override
    public void executer() throws ArcException {
        StaticLoggerDispatcher.info("** executer **", LOGGER);
        
        this.directoryIn = ServiceFileSystemManagement.directoryPhaseEtatOK(this.getDirectoryRoot(), this.envExecution, TraitementPhase.valueOf(previousPhase)) + File.separator;

        // récupération des différentes normes dans la base
        this.listeNorme = Norme.getNormesBase(this.connexion.getCoordinatorConnection(), this.tableNorme);
 
        this.maxParallelWorkers = BDParameters.getInt(this.connexion.getCoordinatorConnection(), "ApiChargementService.MAX_PARALLEL_WORKERS",4);

        // Récupérer la liste des fichiers selectionnés
        StaticLoggerDispatcher.info("Récupérer la liste des fichiers selectionnés", LOGGER);
        setTabIdSource(pilotageListIdsource(this.tablePilTemp, this.currentPhase, TraitementEtat.ENCOURS.toString()));
        
        MultiThreading<ApiChargementService,ThreadChargementService> mt=new MultiThreading<>(this, new ThreadChargementService());
        mt.execute(maxParallelWorkers, getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()), this.envExecution, properties.getDatabaseRestrictedUsername());

    }
    
    
    
}
