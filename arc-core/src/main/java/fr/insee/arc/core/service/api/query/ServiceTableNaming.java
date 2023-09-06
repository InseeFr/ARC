package fr.insee.arc.core.service.api.query;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.utils.FormatSQL;

public class ServiceTableNaming {

	private ServiceTableNaming() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Build a signifiant and collision free temporary table name
	 * 
	 * @param aEnvExecution the sandbox schema
	 * @param aCurrentPhase the phase TraitementPhase that will be used as part of
	 *                      the builded name
	 * @param tableName     the based tablename that will be used as part of the
	 *                      builded name
	 * @param suffix        optionnal suffix added to the temporary name
	 * @return
	 */
	public static String temporaryTableName(String aEnvExecution, String aCurrentPhase, String tableName,
			String... suffix) {

		if (suffix != null && suffix.length > 0) {
			String suffixJoin = String.join("$", suffix);
			return FormatSQL.temporaryTableName(dbEnv(aEnvExecution) + aCurrentPhase + "_" + tableName, suffixJoin);
		} else {
			return FormatSQL.temporaryTableName(dbEnv(aEnvExecution) + aCurrentPhase + "_" + tableName);
		}
	}

	public static String globalTableName(String aEnvExecution, String aCurrentPhase, String tableName) {
		return dbEnv(aEnvExecution) + aCurrentPhase + "_" + tableName;
	}

	/**
	 * Permet la rétro compatibilité pour la migration vers 1 schéma par
	 * envirionnement d'execution
	 * 
	 * @param anEnv
	 * @return
	 */
	public static String dbEnv(String env) {
		return env.replace(".", "_") + ".";
	}


    
    public static ArcPreparedStatementBuilder queryTablesFromPgMetadata()
    {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.TABLE_SCHEMA, "||'.'||", ColumnEnum.TABLE_NAME, SQL.AS, ColumnEnum.TABLE_NAME);
		query.build(SQL.FROM, "information_schema.tables");
		return query;
    }
	
}
