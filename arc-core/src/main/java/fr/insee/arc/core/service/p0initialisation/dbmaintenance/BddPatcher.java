package fr.insee.arc.core.service.p0initialisation.dbmaintenance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.SchemaEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.TableMetadata;
import fr.insee.arc.core.service.p0initialisation.ApiInitialisationService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.format.GitDateFormat;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;

public class BddPatcher {

	/**
	 * Placeholder for the restricted access database account (used by arc services)
	 * in the database scripts located in src/main/ressources/Bdd
	 */
	private static final String USER_RESTRICTED_PLACEHOLDER = "{{userRestricted}}";

	/**
	 * Placeholder for the number of sandboxes in the database scripts located in
	 * src/main/ressources/Bdd
	 */
	private static final String NUMBER_OF_SANDBOXES_PLACEHOLDER = "{{nbSandboxes}}";

	/**
	 * Placeholder for the sandbox environment name (arc_bas1 for example) in the
	 * database scripts located in src/main/ressources/Bdd
	 */
	private static final String SANDBOX_ENVIRONMENT_PLACEHOLDER = "{{envExecution}}";

	private PropertiesHandler properties;

	public BddPatcher() {
		super();
		properties = PropertiesHandler.getInstance();
	}

	/**
	 * inject in sql database initialization scripts the input parameters
	 * 
	 * @param query
	 * @param user
	 * @param nbSandboxes
	 * @param envExecution
	 * @return
	 */
	private static String applyBddScriptParameters(String query, String userRestricted, Integer nbSandboxes,
			String envExecution) {
		if (userRestricted != null) {
			query = query.replace(USER_RESTRICTED_PLACEHOLDER, userRestricted);
		}
		if (nbSandboxes != null) {
			query = query.replace(NUMBER_OF_SANDBOXES_PLACEHOLDER, String.valueOf(nbSandboxes));
		}
		if (envExecution != null) {
			query = query.replace(SANDBOX_ENVIRONMENT_PLACEHOLDER, envExecution);
		}
		return query;
	}

	private static String readBddScript(String scriptName, String userRestricted, Integer nbSandboxes,
			String envExecution) throws ArcException {
		if (ApiInitialisationService.class.getClassLoader().getResourceAsStream(scriptName) != null) {
			try {
				return applyBddScriptParameters(IOUtils.toString(
						ApiInitialisationService.class.getClassLoader().getResourceAsStream(scriptName),
						StandardCharsets.UTF_8), userRestricted, nbSandboxes, envExecution);
			} catch (IOException fileReadExceptione) {
				throw new ArcException(fileReadExceptione, ArcExceptionMessage.DATABASE_INITIALISATION_SCRIPT_FAILED);
			}
		}
		return null;
	}

	public static void executeBddScript(Connection connexion, String scriptName, String userRestricted,
			Integer nbSandboxes, String envExecution) throws ArcException {
		String query;

		if ((query = readBddScript(scriptName, userRestricted, nbSandboxes, envExecution)) != null) {
			UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).executeImmediate(connexion, query);
		}
	}

	private static final String GIT_COMMIT_ID_PARAMETER_KEY = "git.commit.id" + SANDBOX_ENVIRONMENT_PLACEHOLDER;
	private static final String GIT_COMMIT_ID_PARAMETER_VALUE_NOVERSION = "NOVERSION";

	private static String gitCommitIdParameterKey(String... envExecution) {
		if (envExecution == null || envExecution.length == 0) {
			return GIT_COMMIT_ID_PARAMETER_KEY.replace(SANDBOX_ENVIRONMENT_PLACEHOLDER, "");
		}

		return GIT_COMMIT_ID_PARAMETER_KEY.replace(SANDBOX_ENVIRONMENT_PLACEHOLDER, "." + envExecution[0]);
	}

	private static String checkBddScriptVersion(Connection connexion, String... envExecution) {

		return new BDParameters(ArcDatabase.COORDINATOR).getStringNoError(connexion,
				gitCommitIdParameterKey(envExecution), GIT_COMMIT_ID_PARAMETER_VALUE_NOVERSION);
	}

	private static void setBddScriptVersion(Connection connexion, String commitId, String... envExecution) {

		new BDParameters(ArcDatabase.COORDINATOR).setString(connexion, gitCommitIdParameterKey(envExecution), commitId,
				bddParameterDescription(envExecution));
	}

	private static void setBddScriptVersionWithoutDescription(Connection connexion, String commitId,
			String... envExecution) {
		new BDParameters(ArcDatabase.COORDINATOR).setString(connexion, gitCommitIdParameterKey(envExecution), commitId);
	}

	/**
	 * parameter description
	 * 
	 * @param envExecution
	 * @return
	 */
	private static String bddParameterDescription(String... envExecution) {
		if (envExecution == null || envExecution.length == 0) {
			return "parameter.database.version.global";
		} else {
			return "parameter.database.version.sandbox";
		}
	}

	/**
	 * execute the global sql scripts
	 * 
	 * @param connexion
	 * @param properties
	 * @throws ArcException
	 */
	private static void bddScriptGlobalExecutor(Connection connexion, String userNameWithRestrictedRights)
			throws ArcException {

		Integer nbSandboxes = new BDParameters(ArcDatabase.COORDINATOR).getInt(connexion,
				"ApiInitialisationService.nbSandboxes", 8);

		executeBddScript(connexion, "BdD/script_global.sql", userNameWithRestrictedRights, nbSandboxes, null);
		executeBddScript(connexion, "BdD/script_function_integrity.sql", userNameWithRestrictedRights, nbSandboxes,
				null);
		executeBddScript(connexion, "BdD/script_function_utility.sql", userNameWithRestrictedRights, nbSandboxes, null);

		// iterate over each phase and try to load its global script

		for (TraitementPhase t : TraitementPhase.values()) {
			executeBddScript(connexion, "BdD/script_global_phase_" + t.toString().toLowerCase() + ".sql",
					userNameWithRestrictedRights, nbSandboxes, null);
		}
	}

	/**
	 * Execute the sql script for environements
	 * 
	 * @param connexion
	 * @param properties
	 * @param envExecutions
	 * @throws ArcException
	 */
	public static void bddScriptEnvironmentExecutor(Connection connexion, String userNameWithRestrictedRights,
			String[] envExecutions) throws ArcException {
		for (String envExecution : envExecutions) {
			executeBddScript(connexion, "BdD/script_sandbox.sql", userNameWithRestrictedRights, null, envExecution);
		}

		// iterate over each sandbox environment
		for (String envExecution : envExecutions) {
			bddScriptSandboxPhases(connexion, userNameWithRestrictedRights, envExecution);
		}
	}

	/**
	 * create in the sandbox the tables need for each phases
	 * 
	 * @param connexion
	 * @param userNameWithRestrictedRights
	 * @param envExecutions
	 * @throws ArcException
	 */
	private static void bddScriptSandboxPhases(Connection connexion, String userNameWithRestrictedRights,
			String envExecution) throws ArcException {
		// iterate over each phase for its sandbox relating script
		for (TraitementPhase t : TraitementPhase.values()) {
			executeBddScript(connexion, "BdD/script_sandbox_phase_" + t.toString().toLowerCase() + ".sql",
					userNameWithRestrictedRights, null, envExecution);
		}
	}

	/**
	 * Méthode pour initialiser ou patcher la base de données la base de donnée. La
	 * version de la base de données correpond au numéro de commit de git
	 * 
	 * @param connexion
	 */
	public void bddScript(Connection connexion, String... envExecutions) {

		String applicationNewGitVersionDate = properties.getVersionDate();
		String userNameWithRestrictedRights = properties.getDatabaseRestrictedUsername();

		bddScript(applicationNewGitVersionDate, userNameWithRestrictedRights, connexion, envExecutions);

	}

	/**
	 * Méthode pour initialiser ou patcher la base de données la base de donnée.
	 * 
	 * @param databaseOldGitVersion
	 * @param applicationNewGitVersion
	 * @param userNameWithRestrictedRights
	 * @param connexion
	 * @param envExecutions
	 */
	private static void bddScript(String applicationNewGitVersionDate, String userNameWithRestrictedRights,
			Connection connexion, String... envExecutions) {
		// retrieve the old version from the parameter table
		// if param not found, parameter table is created and parameter added
		String databaseOldGitVersionDate = checkBddScriptVersion(connexion, envExecutions);
		
		// if new git version date is strictly older than the old git version date
		// proceed to database patch
		if (GitDateFormat.parse(applicationNewGitVersionDate).compareTo(GitDateFormat.parse(databaseOldGitVersionDate)) > 0)
		{
			
			setBddScriptVersionWithoutDescription(connexion, applicationNewGitVersionDate, envExecutions);

			// global script. Mainly to build the arc schema
			try {

				if (envExecutions == null || envExecutions.length == 0) {
					bddScriptGlobalExecutor(connexion, userNameWithRestrictedRights);
				} else {
					bddScriptEnvironmentExecutor(connexion, userNameWithRestrictedRights, envExecutions);
				}

			} catch (Exception e) {
				setBddScriptVersion(connexion, databaseOldGitVersionDate);
			}

			// set version number when the update scripts are over
			setBddScriptVersion(connexion, applicationNewGitVersionDate, envExecutions);

		}
	}

	public PropertiesHandler getProperties() {
		return properties;
	}

	/**
	 * build and execute query to retrieve tables in postgres metadata
	 * according to a condition defined in the functional interface
	 * @param connexion
	 * @param envExecution
	 * @param condition
	 * @return
	 * @throws ArcException
	 */
	private static List<String> retrieveTablesFromSchema(Connection connexion, String envExecution, Function<String, ArcPreparedStatementBuilder> condition)
			throws ArcException {
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(TableMetadata.queryTablesFromPgMetadata());
		query.append(condition.apply(envExecution));
	
		return new GenericBean(UtilitaireDao.get(0).executeRequest(connexion, query))
				.getColumnValues(ColumnEnum.TABLE_NAME.getColumnName());
		
	}
	
		
	
	/**
	 * Build the condition to retrieve tables that contains models
	 * @param envExecution
	 * @return
	 */
	private static ArcPreparedStatementBuilder conditionToRetrieveModelTablesInSchema(String envExecution) {
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.WHERE);

		// family tables			
		query.build(ColumnEnum.TABLE_SCHEMA, "=", query.quoteText(SchemaEnum.ARC_METADATA.getSchemaName()));
		query.build(SQL.AND);
		query.build(ColumnEnum.TABLE_NAME, SQL.IN, "(");
		query.build(query.quoteText(ViewEnum.IHM_MOD_TABLE_METIER.getTableName()), ",");
		query.build(query.quoteText(ViewEnum.IHM_MOD_VARIABLE_METIER.getTableName()), ",");
		query.build(query.quoteText(ViewEnum.IHM_FAMILLE.getTableName()));

		query.build(")");
				
		return query;		
	}

	/**
	 * Build the condition to retrieve tables that contains models
	 * @param envExecution
	 * @return
	 */
	private static ArcPreparedStatementBuilder conditionToRetrieveMappingTablesInSchema(String envExecution) {
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.WHERE);
		
		// mapping tables
		query.build(ColumnEnum.TABLE_SCHEMA, "=", query.quoteText(envExecution));
		query.build(SQL.AND);
		query.build(ColumnEnum.TABLE_NAME, SQL.LIKE, query.quoteText(TraitementPhase.MAPPING.toString().toLowerCase()+"\\_%ok"));
				
		return query;		
	}
	
	/**
	 * Build the condition to retrieve tables that contains rules
	 * @param envExecution
	 * @return
	 */
	private static ArcPreparedStatementBuilder conditionToRetrieveRulesTablesInSchema(String envExecution) {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.WHERE);
		query.build(ColumnEnum.TABLE_SCHEMA, "=", query.quoteText(envExecution));
		query.build(SQL.AND);
		query.build(ColumnEnum.TABLE_NAME, SQL.IN, "(");

		query.build(query.quoteText(ViewEnum.MOD_TABLE_METIER.getTableName()), ",");
		query.build(query.quoteText(ViewEnum.MOD_VARIABLE_METIER.getTableName()), ",");

		// must return the table of rules for phase
		for (TraitementPhase p : TraitementPhase.RECEPTION.nextPhases()) {
			query.build(query.quoteText(p.tableRegleOfPhaseInSandbox()), ",");
		}

		query.build(query.quoteText(ViewEnum.NORME.getTableName()), ",");
		query.build(query.quoteText(ViewEnum.CALENDRIER.getTableName()), ",");
		query.build(query.quoteText(ViewEnum.JEUDEREGLE.getTableName()));

		// close IN
		query.build(")");
		
		
		return query;
	}

	// return external tables used in rules
	public static List<String> retrieveExternalTablesUsedInRules(Connection connexion, String envExecution)
			throws ArcException {
		
		// generate a sql expression with relevant the columns concatenation of rules table
		ArcPreparedStatementBuilder query;
		query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT);
		query.build(ColumnEnum.TABLE_SCHEMA, "||'.'||", ColumnEnum.TABLE_NAME, SQL.AS, ColumnEnum.TABLE_NAME);
		query.build(",", "'concat('||string_agg(");
		// table alias is use in next query for exists subquery
		query.build("'", ViewEnum.T1, ".", "'", "||");
		query.build(ColumnEnum.COLUMN_NAME,",',')||')' as sql_cols");
		query.build(SQL.FROM, "information_schema.columns");
		query.build(conditionToRetrieveRulesTablesInSchema(envExecution));
		query.build(SQL.AND, ColumnEnum.COLUMN_NAME, "!=", query.quoteText(ColumnEnum.COMMENTAIRE));
		query.build(SQL.AND, ColumnEnum.COLUMN_NAME, "!=", query.quoteText(ColumnEnum.ID_NORME));
		query.build(SQL.AND, ColumnEnum.COLUMN_NAME, "!=", query.quoteText(ColumnEnum.PERIODICITE));
		query.build(SQL.AND, ColumnEnum.COLUMN_NAME, "!=", query.quoteText(ColumnEnum.VALIDITE_INF));
		query.build(SQL.AND, ColumnEnum.COLUMN_NAME, "!=", query.quoteText(ColumnEnum.VALIDITE_SUP));
		query.build(SQL.AND, ColumnEnum.COLUMN_NAME, "!=", query.quoteText(ColumnEnum.VERSION));
		query.build(SQL.GROUP_BY, ColumnEnum.TABLE_SCHEMA, "||'.'||", ColumnEnum.TABLE_NAME);

		Map<String, List<String>> result = new GenericBean(
				UtilitaireDao.get(0).executeRequest(connexion, query)).mapContent();

		// search if a nomenclature table is quoted in the columns concatenation of rules tables
		// if so, it must be returned
		query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, SQL.DISTINCT, "u||'.'||", ColumnEnum.NOM_TABLE, SQL.AS, ColumnEnum.TABLE_NAME);
		query.build(SQL.FROM, new DataObjectService().getView(ViewEnum.IHM_NMCL), SQL.AS,ViewEnum.T2);
		query.build(",", SQL.UNNEST, "(ARRAY[", query.quoteText(envExecution), ",", query.quoteText(SchemaEnum.ARC_METADATA.getSchemaName()), "]) u");
		query.build(SQL.WHERE, SQL.FALSE);
		
		for (int i=0; i<result.get("sql_cols").size(); i++)
		{
			
			query.build(SQL.OR, SQL.EXISTS, "(");
			query.build(SQL.SELECT, SQL.FROM, result.get(ColumnEnum.TABLE_NAME.getColumnName()).get(i), SQL.AS, ViewEnum.T1);
			query.build(SQL.WHERE, result.get("sql_cols").get(i), SQL.LIKE, "'%'||", ViewEnum.T2, ".", ColumnEnum.NOM_TABLE, "||'%'" );
			query.build(")");
		}
		
		return new GenericBean(UtilitaireDao.get(0).executeRequest(connexion, query))
				.getColumnValues(ColumnEnum.TABLE_NAME.getColumnName());
		
	}
	

	/**
	 * return a query that retrieve the rules tables in schema This table will be
	 * copied to the executor
	 * 
	 * @param envExecution
	 * @throws ArcException
	 */
	public static List<String> retrieveRulesTablesFromSchema(Connection connexion, String envExecution)
			throws ArcException {
		return retrieveTablesFromSchema(connexion, envExecution, BddPatcher::conditionToRetrieveRulesTablesInSchema );
	}
	
	public static List<String> retrieveModelTablesFromSchema(Connection connexion, String envExecution)
			throws ArcException {
		return retrieveTablesFromSchema(connexion, envExecution, BddPatcher::conditionToRetrieveModelTablesInSchema );
	}
	
	public static List<String> retrieveMappingTablesFromSchema(Connection connexion, String envExecution)
			throws ArcException {
		return retrieveTablesFromSchema(connexion, envExecution, BddPatcher::conditionToRetrieveMappingTablesInSchema );
	}

}
