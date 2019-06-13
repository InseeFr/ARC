package fr.insee.arc_essnet.core.service;

import java.sql.Connection;

public class ApiTranfoTableService extends AbstractPhaseService  implements IApiServiceWithoutOutputTable {

    public ApiTranfoTableService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
            String... paramBatch) {
        super(aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
        
    }
    public ApiTranfoTableService(Connection connexion, String anParametersEnvironment, String aEnvExecution,
	    String aDirectoryRoot, String string, Integer valueOf) {
	// TODO Auto-generated constructor stub
    }
    @Override
    public void process() throws Exception {
        // TODO Auto-generated method stub

    }

}
