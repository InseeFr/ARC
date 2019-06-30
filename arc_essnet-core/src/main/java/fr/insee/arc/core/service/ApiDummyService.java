package fr.insee.arc.core.service;

import java.sql.Connection;

/**
 * Inutile d'un point de vu métier, mais utile techniquement. Permet d'avoir une phase précédante même pour la phase initiale
 * @author S4lwo8
 *
 */
public class ApiDummyService extends AbstractPhaseService implements IApiServiceWithoutOutputTable {


    public ApiDummyService() {
	super();
    }

    public ApiDummyService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
            String... paramBatch) {
        super(aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);

    }

    public ApiDummyService(Connection connexion, String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
            String... paramBatch) {
        super(connexion,aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);

    }

    @Override
    public void process() throws Exception {
	// TODO Auto-generated method stub
	
    }
}
