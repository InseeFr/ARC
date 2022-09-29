package fr.insee.arc.core.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.databaseobjetcs.ColumnEnum;
import fr.insee.arc.core.service.engine.mapping.RegleMappingFactory;
import fr.insee.arc.core.service.thread.ThreadMappingService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;


/**
 * Le mapping récupère les données filtrées (voir {@link ApiFiltrageService}) qui sont dans la table {@code <environnement>_filtrage_ok} et
 * transforme les variables administratives qu'elle contient en variables statistiques réparties dans des tables métier de nom
 * {@code <environnement>_mapping_<application_cliente>}_ok}, en respectant un modèle relationnel :<br/>
 * 1. Le modèle relationnel est dépendant de la famille (donc de l'application cliente qui récupère le produit du mapping), et est stocké
 * dans les tables {@code <environnement>_mod_table_metier} et {@code <environnement>_mod_variable_metier}<br/>
 * 2. Le modèle relationnel décrit dans ces tables décrit également un type de consolidation qui impacte les règles de gestion du mapping.<br/>
 * 3. Les transformations des variables administratives en variables statistiques sont décrites dans la table
 * {@code <environnement>_mapping_regle}.<br/>
 * Dans l'ordre, ce service :<br/>
 * 1. Instancie le contexte de travail (voir {@link ApiService#initialiser()} et {@link #preparerExecution()}).<br/>
 * 2. Exécute les traitements :<br/>
 * 2.1. Liste les jeux de règles et itère sur eux.<br/>
 * 2.2. Construit la requête de mapping pour ce jeu de règles.<br/>
 * 2.3. Construit la liste de fichiers associés au jeu de règles.<br/>
 * 2.4. Stocke la requête de mapping pour ce jeu de règles et ce fichier dans un buffer à requêtes {@link RequeteMappingCalibree}.<br/>
 * 3. Sauvegarde le contexte de travail (voir {@link ApiService#finaliser()})<br/>
 * Le buffer à requête exécute toutes les requêtes qu'il contient chaque fois qu'un nombre total de caractères est dépassé.
 */
@Component
public class ApiMappingService extends ApiService {
	private static final Logger logger = LogManager.getLogger(ApiMappingService.class);
        
    public ApiMappingService() {
        super();
    }
    
    private static final String PREFIX_IDENTIFIANT_RUBRIQUE = "i_";

    protected RegleMappingFactory regleMappingFactory;

    /**
     * @param anParametersEnvironment
     *            inutile
     * @param anEnvironnementExecution
     * @param aDirectoryRoot
     *            inutile
     * @param aCurrentPhase
     * @param aNbEnr
     */
    public ApiMappingService(String aCurrentPhase, String anParametersEnvironment, String anEnvironnementExecution, String aDirectoryRoot,
            Integer aNbEnr, String... paramBatch) {
        super(aCurrentPhase, anParametersEnvironment, anEnvironnementExecution, null, aNbEnr, paramBatch);
    }

    /**
     * Un jeu de règles = un lot de traitement
     */
    @Override
    public void executer() throws ArcException {
    	
        this.maxParallelWorkers = BDParameters.getInt(this.connexion, "MappingService.MAX_PARALLEL_WORKERS",4);
        
        // récupère le nombre de fichier à traiter
        this.setTabIdSource(recuperationIdSource(getPreviousPhase()));
        
        int nbFichier = getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).size();
        
        Connection connextionThread = null;
        ArrayList<ThreadMappingService> threadList = new ArrayList<>();
        ArrayList<Connection> connexionList = ApiService.prepareThreads(maxParallelWorkers, null, this.envExecution, properties.getDatabaseRestrictedUsername());
        int currentIndice = 0;

        StaticLoggerDispatcher.info("** Generation des threads pour le mapping **", logger);
        for (currentIndice = 0; currentIndice < nbFichier; currentIndice++) {
            
            if (currentIndice%10 == 0) {
                StaticLoggerDispatcher.info("Mapping fichier " + currentIndice + "/" + nbFichier, logger);
            }
            
            connextionThread = chooseConnection(connextionThread, threadList, connexionList);
            this.currentIdSource = getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(currentIndice);
            
            ThreadMappingService r = new ThreadMappingService( connextionThread, currentIndice, this);
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

    public static String getPrefixidentifiantrubrique() {
        return PREFIX_IDENTIFIANT_RUBRIQUE;
    }

}
