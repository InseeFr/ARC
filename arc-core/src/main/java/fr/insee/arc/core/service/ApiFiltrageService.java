package fr.insee.arc.core.service;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.service.thread.ThreadFiltrageService;
import fr.insee.arc.utils.structure.tree.HierarchicalView;

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
public class ApiFiltrageService extends AbstractThreadRunnerService<ThreadFiltrageService>
implements IApiServiceWithOutputTable {

    private static final Logger LOGGER = Logger.getLogger(ApiFiltrageService.class);
    protected String seuilExclusion;
    protected HierarchicalView normeToPeriodiciteToValiditeInfToValiditeSupToRegle;
    private static final Class<ThreadFiltrageService> THREAD_TYPE = ThreadFiltrageService.class ;

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
        super(THREAD_TYPE, aCurrentPhase, anParametersEnvironment, aEnvExecution, null, aNbEnr, paramBatch);

        // fr.insee.arc.threads.filtrage
        this.nbThread = 3;

    }
    public ApiFiltrageService(Connection connexion, String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
        super(connexion,THREAD_TYPE, aCurrentPhase, anParametersEnvironment, aEnvExecution, null, aNbEnr, paramBatch);

        // fr.insee.arc.threads.filtrage
        this.nbThread = 3;

    }


}
