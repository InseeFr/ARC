package fr.insee.arc.core.service.p5mapping;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.thread.MultiThreading;
import fr.insee.arc.core.service.p4controle.ApiControleService;
import fr.insee.arc.core.service.p5mapping.dao.MappingQueriesFactory;
import fr.insee.arc.core.service.p5mapping.thread.ThreadMappingService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;


/**
 * Le mapping récupère les données du controle (voir {@link ApiControleService}) qui sont dans les tables {@code <environnement>.controle_ok_child} et
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
        
    public ApiMappingService() {
        super();
    }
    
    private static final String PREFIX_IDENTIFIANT_RUBRIQUE = "i_";

    protected MappingQueriesFactory regleMappingFactory;

    /**
     * @param anParametersEnvironment
     *            inutile
     * @param anEnvironnementExecution
     * @param aDirectoryRoot
     *            inutile
     * @param aCurrentPhase
     * @param aNbEnr
     */
    public ApiMappingService(TraitementPhase aCurrentPhase, String anEnvironnementExecution,
            Integer aNbEnr, String paramBatch) {
        super(aCurrentPhase, anEnvironnementExecution, aNbEnr, paramBatch);
    }

    /**
     * Un jeu de règles = un lot de traitement
     */
    @Override
    public void executer() throws ArcException {
    	
		PropertiesHandler properties = PropertiesHandler.getInstance();
    	
        BDParameters bdParameters=new BDParameters(ArcDatabase.COORDINATOR);

        this.maxParallelWorkers = bdParameters.getInt(this.connexion.getCoordinatorConnection(), "MappingService.MAX_PARALLEL_WORKERS",4);
        
        // récupère le nombre de fichier à traiter
        this.tabIdSource = recuperationIdSource();
      
        MultiThreading<ApiMappingService,ThreadMappingService> mt=new MultiThreading<>(this, new ThreadMappingService());
        mt.execute(maxParallelWorkers, getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()), this.envExecution, properties.getDatabaseRestrictedUsername());

    }

    public static String getPrefixidentifiantrubrique() {
        return PREFIX_IDENTIFIANT_RUBRIQUE;
    }

}
