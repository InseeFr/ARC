package fr.insee.arc_essnet.core.service;

import java.sql.Connection;
import java.util.List;

import org.springframework.stereotype.Component;

import fr.insee.arc_essnet.core.model.Norme;
import fr.insee.arc_essnet.core.service.thread.ThreadIdentifyService;

/**
 * This phase determine the norme, and the validity of abth of files
 * @author S4lwo8
 *
 */
@Component
public class ApiIdentifyService extends AbstractThreadRunnerService<ThreadIdentifyService> implements IApiServiceWithoutOutputTable{

    private static final Class<ThreadIdentifyService> THREAD_TYPE = ThreadIdentifyService.class ;

    protected List<Norme> normList;
    
    public ApiIdentifyService() {
	super();
    }
    
    public ApiIdentifyService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution,
	    String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	super(THREAD_TYPE, aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);


	// fr.insee.arc.threads.chargement
	this.nbThread = 3;

	// Get all normes in database
	this.normList = getAllNorms();
    }
    public ApiIdentifyService(Connection connexion,String aCurrentPhase, String anParametersEnvironment, String aEnvExecution,
	    String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	super(connexion,THREAD_TYPE, aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
	
	// fr.insee.arc.threads.chargement
	this.nbThread = 3;
	
	// Get all normes in database
	this.normList = getAllNorms();

    }


}
