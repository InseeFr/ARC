package fr.insee.arc.core.service;

import java.sql.Connection;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.service.thread.ThreadNormageService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.LoggerDispatcher;


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

    private static final Logger logger = Logger.getLogger(ApiNormageService.class);
    
    public ApiNormageService() {
        super();
    }

    private int currentIndice;
    
    protected String separator = ",";

    public ApiNormageService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
            String... paramBatch) {
        super(aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
        this.setTableNorme(dbEnv(this.getEnvExecution()) + TraitementTableParametre.NORME);

//        this.tableNormageOK = ApiService.globalTableName(this.getEnvExecution(), this.getCurrentPhase(), TraitementEtat.OK.toString());
//        this.tableNormageKO = ApiService.globalTableName(this.getEnvExecution(), this.getCurrentPhase(), TraitementEtat.KO.toString());
    }

    @Override
    public void executer() throws Exception {
        LoggerDispatcher.info("** executer **", LOGGER);
        
        this.MAX_PARALLEL_WORKERS = BDParameters.getInt(this.connexion, "ApiNormageService.MAX_PARALLEL_WORKERS",4);
        
        long dateDebut = java.lang.System.currentTimeMillis() ;
        
        // récupère le nombre de fichier à traiter
        this.setTabIdSource(recuperationIdSource(getPreviousPhase()));
        
        int nbFichier = getTabIdSource().get(ID_SOURCE).size();
        
        Connection connectionThread = null;
        
        // Pool de thread
        ArrayList<ThreadNormageService> threadList = new ArrayList<ThreadNormageService>();
        
        // Pool de connexion
        ArrayList<Connection> connexionList = ApiService.prepareThreads(MAX_PARALLEL_WORKERS, null, this.envExecution);
        currentIndice = 0;

        LoggerDispatcher.info("** Generation des threads pour le normage **", logger);
        for (currentIndice = 0; currentIndice < nbFichier; currentIndice++) {
            
            if (currentIndice%10 == 0) {
                LoggerDispatcher.info("Normage fichier " + currentIndice + "/" + nbFichier, logger);
            }
            
            connectionThread = chooseConnection(connectionThread, threadList, connexionList);
            this.currentIdSource = getTabIdSource().get("id_source").get(currentIndice);
            
            ThreadNormageService r = new ThreadNormageService( connectionThread, currentIndice, this);
            threadList.add(r);
            r.start();
            
            waitForThreads2(MAX_PARALLEL_WORKERS, threadList, connexionList);


        }

        LoggerDispatcher.info("** Attente de la fin des threads **", logger);
        waitForThreads2(0, threadList, connexionList);


        LoggerDispatcher.info("** Fermeture des connexions **", logger);
        for (Connection connection : connexionList) {
            connection.close();
        }

        long dateFin= java.lang.System.currentTimeMillis() ;
        LoggerDispatcher.info("Temp normage des "+ nbFichier+" fichiers : " + (int)Math.round((dateFin-dateDebut)/1000F)+" sec", LOGGER);
        
    }



   

}
