package fr.insee.arc.core.service.api;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.thread.MultiThreading;
import fr.insee.arc.core.service.thread.ThreadFiltrageService;
import fr.insee.arc.core.util.BDParameters;
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
    public ApiFiltrageService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr, String paramBatch) {
        super(aCurrentPhase, anParametersEnvironment, aEnvExecution, null, aNbEnr, paramBatch);
    }

 

    /**
     * Exécute le mapping de bout en bout
     * @throws ArcException 
     */
    public void executer() throws ArcException {
        
        this.maxParallelWorkers = BDParameters.getInt(this.connexion.getCoordinatorConnection(), "ApiFiltrageService.MAX_PARALLEL_WORKERS",2);
    	
        this.setTabIdSource(recuperationIdSource(getPreviousPhase()));
        
        MultiThreading<ApiFiltrageService,ThreadFiltrageService> mt=new MultiThreading<>(this, new ThreadFiltrageService());
        mt.execute(maxParallelWorkers, getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()), this.envExecution, properties.getDatabaseRestrictedUsername());

    }

}
