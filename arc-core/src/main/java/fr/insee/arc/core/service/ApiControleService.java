package fr.insee.arc.core.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.service.engine.controle.ServiceJeuDeRegle;
import fr.insee.arc.core.service.thread.ThreadControleService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;


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
	private static final Logger logger = LogManager.getLogger(ApiControleService.class);

    public ApiControleService() {
        super();
    }

    public ApiControleService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
            String... paramBatch) {
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
    public void executer() throws Exception {

        StaticLoggerDispatcher.info("** executer **", LOGGER_APISERVICE);

        this.maxParallelWorkers = BDParameters.getInt(this.connexion, "ApiControleService.MAX_PARALLEL_WORKERS",3);

        
        long dateDebut = java.lang.System.currentTimeMillis() ;
        // Initilisation de la table de pilotage

        // récupère le nombre de fichier à traiter
        this.setTabIdSource(recuperationIdSource(getPreviousPhase()));
        int nbFichier = getTabIdSource().get(ID_SOURCE).size();
        Connection connextionThread = null;
        ArrayList<ThreadControleService> threadList = new ArrayList<ThreadControleService>();
        ArrayList<Connection> connexionList = ApiService.prepareThreads(maxParallelWorkers, null, this.envExecution, properties.getDatabaseRestrictedUsername());
        int currentIndice = 0;

        StaticLoggerDispatcher.info("** Generation des threads pour le contrôle **", logger);

        for (currentIndice = 0; currentIndice < nbFichier; currentIndice++) {

            if (currentIndice % 10 == 0) {
                StaticLoggerDispatcher.info("contrôle fichier " + currentIndice + "/" + nbFichier, logger);
            }

            connextionThread = chooseConnection(connextionThread, threadList, connexionList);
            this.currentIdSource = getTabIdSource().get("id_source").get(currentIndice);

            ThreadControleService r = new ThreadControleService(connextionThread, currentIndice, this);
            threadList.add(r);
            r.start();
            waitForThreads2(maxParallelWorkers, threadList, connexionList);

        }

        StaticLoggerDispatcher.info("** Attente de la fin des threads **", logger);
        
        waitForThreads2(0, threadList, connexionList);

        StaticLoggerDispatcher.info("** Fermeture des connexions **", logger);
        for (Connection connection : connexionList) {
            connection.close();
            
        }
        long dateFin= java.lang.System.currentTimeMillis() ;

        StaticLoggerDispatcher.info("Temp chargement des "+ nbFichier+" fichiers : " + (int)Math.round((dateFin-dateDebut)/1000F)+" sec", LOGGER_APISERVICE);


    }

    /**
     * Méthode pour controler une table
     *
     * @param connexion
     *
     * @param tableControle
     *            la table à controler
     *
     * @throws SQLException
     */
    public static void executeABlanc(Connection connexion, String env, String phase, String tableControle, ServiceJeuDeRegle sjdrA, ArrayList<JeuDeRegle> listJdrA) throws Exception {
        StaticLoggerDispatcher.info("** execute CONTROLE sur la table : " + tableControle + " **", logger);
        for (JeuDeRegle jdr : listJdrA) {
            sjdrA.executeJeuDeRegle(connexion, jdr, tableControle, null);
        }

    }
    
}
