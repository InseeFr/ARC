package fr.insee.arc.core.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.databaseobjects.ColumnEnum;
import fr.insee.arc.core.service.thread.ThreadFiltrageService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.tree.HierarchicalView;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;

/**
 * La table {@code <environnement>_controle_ok} contient les données chargées, normées et contrôlées.<br/>
 * L'étape de filtrage/exclusion scanne la table {@code <environnement>_filtrage_regle} à la recherche d'une condition SQL d'exclusion, et
 * génère deux produits :<br/>
 * 1. La table {@code <environnement>_filtrage_ko} qui contient les lignes de la table {@code <environnement>_controle_ok}, qui vérifient la
 * condition (lignes exclues).<br/>
 * 2. La table {@code <environnement>_filtrage_ok} qui contient les lignes de la table {@code <environnement>_controle_ok}, qui vérifient ne
 * vérifient pas la condition (lignes gardées).<br/>
 *
 *
 *
 */
@Component
public class ApiFiltrageService extends ApiService implements IConstanteCaractere {
    private static final Logger logger = LogManager.getLogger(ApiFiltrageService.class);

        
    protected String seuilExclusion;
    protected HierarchicalView normeToPeriodiciteToValiditeInfToValiditeSupToRegle;
    
    public ApiFiltrageService() {
        super();
    }

    /**
     *
     * @param aCurrentPhase
     * @param anParametersEnvironment
     * @param aEnvExecution
     * @param aDirectoryRoot
     * @param aNbEnr
     */
    public ApiFiltrageService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
        super(aCurrentPhase, anParametersEnvironment, aEnvExecution, null, aNbEnr, paramBatch);
    }

 

    /**
     * Exécute le mapping de bout en bout
     * @throws ArcException 
     */
    public void executer() throws ArcException {
        
        this.maxParallelWorkers = BDParameters.getInt(this.connexion, "ApiFiltrageService.MAX_PARALLEL_WORKERS",2);
    	
        this.setTabIdSource(recuperationIdSource(getPreviousPhase()));
        int nbFichier = getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).size();
        
        Connection connextionThread = null;
        ArrayList<ThreadFiltrageService> threadList = new ArrayList<>();
        ArrayList<Connection> connexionList = ApiService.prepareThreads(maxParallelWorkers, null, this.envExecution, properties.getDatabaseRestrictedUsername());
        int currentIndice = 0;

        StaticLoggerDispatcher.info("** Generation des threads pour le filtrage **", logger);
        
        for (currentIndice = 0; currentIndice < nbFichier; currentIndice++) {

            if (currentIndice % 10 == 0) {
                StaticLoggerDispatcher.info("filtrage fichier " + currentIndice + "/" + nbFichier, logger);
            }

            connextionThread = chooseConnection(connextionThread, threadList, connexionList);
            this.currentIdSource = getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(currentIndice);

            ThreadFiltrageService r = new ThreadFiltrageService(connextionThread, currentIndice, this);
            threadList.add(r);
            r.start();
            waitForThreads2(maxParallelWorkers, threadList, connexionList);

        }

        StaticLoggerDispatcher.info("** Attente de la fin des threads **", logger);
        waitForThreads2(0, threadList, connexionList);



        StaticLoggerDispatcher.info("** Fermeture des connexions **", logger);
        for (Connection connection : connexionList) {
            try {
				connection.close();
			} catch (SQLException e) {
				throw new ArcException("Error in closing thread connections",e);
			}
        }


    }

}
