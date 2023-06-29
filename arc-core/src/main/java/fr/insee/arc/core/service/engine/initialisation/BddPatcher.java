package fr.insee.arc.core.service.engine.initialisation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

import org.apache.commons.io.IOUtils;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.api.ApiInitialisationService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class BddPatcher {

	
	/**
	 * Placeholder for the restricted access database account (used by arc services)
	 * in the database scripts located in src/main/ressources/Bdd
	 */
	private static final String USER_RESTRICTED_PLACEHOLDER="{{userRestricted}}";
	
	/**
	 * Placeholder for the number of sandboxes
	 * in the database scripts located in src/main/ressources/Bdd
	 */
	private static final String NUMBER_OF_SANDBOXES_PLACEHOLDER="{{nbSandboxes}}";
	
	
	/**
	 * Placeholder for the sandbox environment name (arc_bas1 for example) 
	 * in the database scripts located in src/main/ressources/Bdd
	 */
	private static final String SANDBOX_ENVIRONMENT_PLACEHOLDER="{{envExecution}}";
	
	private PropertiesHandler properties;
		
	public BddPatcher() {
		super();
		properties = PropertiesHandler.getInstance();
	}

	/**
	 * inject in sql database initialization scripts the input parameters
	 * @param query
	 * @param user
	 * @param nbSandboxes
	 * @param envExecution
	 * @return
	 */
	private static String applyBddScriptParameters (String query, String userRestricted, Integer nbSandboxes, String envExecution)
	{
		if (userRestricted!=null)
		{
			query=query.replace(USER_RESTRICTED_PLACEHOLDER, userRestricted);
		}
		if (nbSandboxes!=null)
		{
			query=query.replace(NUMBER_OF_SANDBOXES_PLACEHOLDER, String.valueOf(nbSandboxes));
		}
		if (envExecution!=null)
		{
			query=query.replace(SANDBOX_ENVIRONMENT_PLACEHOLDER, envExecution.replace(".","_"));
		}
		return query;
	}
	
	private static String readBddScript (String scriptName, String userRestricted, Integer nbSandboxes, String envExecution) throws ArcException
	{
			if (ApiInitialisationService.class.getClassLoader().getResourceAsStream(scriptName)!=null)
			{
				try {
					return applyBddScriptParameters(IOUtils.toString(ApiInitialisationService.class.getClassLoader().getResourceAsStream(scriptName), StandardCharsets.UTF_8), userRestricted, nbSandboxes, envExecution);
				} catch (IOException fileReadExceptione) {
					throw new ArcException(fileReadExceptione, ArcExceptionMessage.DATABASE_INITIALISATION_SCRIPT_FAILED);
				}
			}
		return null;
	}
	
	private static void executeBddScript (Connection connexion, String scriptName, String userRestricted, Integer nbSandboxes, String envExecution) throws ArcException
	{
		String query;

		if ((query=readBddScript(scriptName, userRestricted, nbSandboxes, envExecution))!=null)
		{
				UtilitaireDao.get(ArcDatabase.META_DATA.getIndex()).executeImmediate(connexion,query);
		}
	}
	
	
	private static final String GIT_COMMIT_ID_PARAMETER_KEY="git.commit.id"+SANDBOX_ENVIRONMENT_PLACEHOLDER;
	private static final String GIT_COMMIT_ID_PARAMETER_VALUE_NOVERSION="NOVERSION";
	
	private static String gitCommitIdParameterKey(String...envExecution)
	{
		if (envExecution==null || envExecution.length==0)
		{
			return GIT_COMMIT_ID_PARAMETER_KEY.replace(SANDBOX_ENVIRONMENT_PLACEHOLDER,"");
		}
			
		return GIT_COMMIT_ID_PARAMETER_KEY.replace(SANDBOX_ENVIRONMENT_PLACEHOLDER,"."+envExecution[0]);
	}

	
	private static String checkBddScriptVersion (Connection connexion, String... envExecution)
	{
		
		return new BDParameters(ArcDatabase.META_DATA).getString(connexion, gitCommitIdParameterKey(envExecution),GIT_COMMIT_ID_PARAMETER_VALUE_NOVERSION);
	}


	private static void setBddScriptVersion (Connection connexion, String commitId, String... envExecution)
	{
		
		new BDParameters(ArcDatabase.META_DATA).setString(connexion, gitCommitIdParameterKey(envExecution), commitId, bddParameterDescription(envExecution));
	}
	
	private static void setBddScriptVersionWithoutDescription (Connection connexion, String commitId, String... envExecution)
	{
		new BDParameters(ArcDatabase.META_DATA).setString(connexion, gitCommitIdParameterKey(envExecution), commitId);
	}

	/**
	 * parameter description
	 * @param envExecution
	 * @return
	 */
	private static String bddParameterDescription(String...envExecution)
	{
		if (envExecution==null || envExecution.length==0)
		{
			return "parameter.database.version.global"; 
		}
		else
		{
			return "parameter.database.version.sandbox"; 
		}
	}
	
	
	/**
	 * execute the global sql scripts
	 * @param connexion
	 * @param properties
	 * @throws ArcException
	 */
	private static void bddScriptGlobalExecutor(Connection connexion, String userNameWithRestrictedRights) throws ArcException
	{
		
		Integer nbSandboxes = new BDParameters(ArcDatabase.META_DATA).getInt(connexion, "ApiInitialisationService.nbSandboxes", 8);
		
		executeBddScript(connexion, "BdD/script_global.sql", userNameWithRestrictedRights, nbSandboxes, null);
		executeBddScript(connexion, "BdD/script_function.sql", userNameWithRestrictedRights, nbSandboxes,
				null);

		// iterate over each phase and try to load its global script

		for (TraitementPhase t : TraitementPhase.values()) {
			executeBddScript(connexion, "BdD/script_global_phase_" + t.toString().toLowerCase() + ".sql",
					userNameWithRestrictedRights, nbSandboxes, null);
		}
	}
	
	
	/**
	 * Execute the sql script for environements
	 * @param connexion
	 * @param properties
	 * @param envExecutions
	 * @throws ArcException
	 */
	private static void bddScriptEnvironmentExecutor(Connection connexion, String userNameWithRestrictedRights, String[] envExecutions) throws ArcException
	{
		for (String envExecution: envExecutions)
		{
			executeBddScript(connexion, "BdD/script_sandbox.sql", userNameWithRestrictedRights, null, envExecution);
		}
	    
	    // iterate over each sandbox environment
		for (String envExecution: envExecutions)
	    {
	        // iterate over each phase for its sandbox relating script
			for (TraitementPhase t : TraitementPhase.values()) {
	            executeBddScript(connexion, "BdD/script_sandbox_phase_"+t.toString().toLowerCase()+".sql", userNameWithRestrictedRights, null, envExecution);
			}
	    }
	}
	
    /**
     * Méthode pour initialiser ou patcher la base de données la base de donnée.
     * La version de la base de données correpond au numéro de commit de git 
     * @param connexion
     */
	public void bddScript(Connection connexion, String...envExecutions) {
		
		String applicationNewGitVersion=properties.getGitCommitId();
		String userNameWithRestrictedRights=properties.getDatabaseRestrictedUsername();
		
		bddScript(applicationNewGitVersion, userNameWithRestrictedRights, connexion, envExecutions);
		
	}	
	
	/**
	 * Méthode pour initialiser ou patcher la base de données la base de donnée.
	 * @param databaseOldGitVersion
	 * @param applicationNewGitVersion
	 * @param userNameWithRestrictedRights
	 * @param connexion
	 * @param envExecutions
	 */
	private static void bddScript(String applicationNewGitVersion, String userNameWithRestrictedRights, Connection connexion, String...envExecutions)
	{
		// retrieve the old version from the parameter table
		// if param not found, parameter table is created and parameter added
		String databaseOldGitVersion=checkBddScriptVersion(connexion, envExecutions);
		
		// if database registered git number is not the same as the application git number
		
				if (!databaseOldGitVersion.equals(applicationNewGitVersion)) {

					setBddScriptVersionWithoutDescription(connexion,applicationNewGitVersion, envExecutions);
					

					// global script. Mainly to build the arc schema
					try {
						
						if (envExecutions==null || envExecutions.length==0)
						{
							bddScriptGlobalExecutor(connexion,userNameWithRestrictedRights);
						}
						else
						{
							bddScriptEnvironmentExecutor(connexion,userNameWithRestrictedRights,envExecutions);
						}
						
					} catch (Exception e) {
						setBddScriptVersion(connexion,databaseOldGitVersion);
					}
					
					// set version number when the update scripts are over
					setBddScriptVersion(connexion,applicationNewGitVersion, envExecutions);
					
				}
	}

	public PropertiesHandler getProperties() {
		return properties;
	}

}
