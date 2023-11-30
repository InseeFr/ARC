package fr.insee.arc.core.service.global.dao;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.Delimiters;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.utils.FormatSQL;

public class TableNaming {

	private TableNaming() {
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
	public static String temporaryTableName(String aEnvExecution, TraitementPhase aCurrentPhase, ViewEnum table) {
		return ViewEnum.getFullName(aEnvExecution, FormatSQL.temporaryTableName(aCurrentPhase + "_" + table));
	}

	public static String phaseDataTableName(String aEnvExecution, TraitementPhase aPhase, TraitementEtat etat) {
		return ViewEnum.getFullName(aEnvExecution, aPhase + "_" + etat.toString());
	}

	/**
	 * Build a table name from a several informations
	 * schema.token#1_token#2_..._token#n_suffix
	 * @param schema
	 * @param mainTable
	 * @param tokens
	 * @return
	 */
	public static String buildTableNameWithTokens(String schema, String mainSuffix, Object... tokens)
	{
		StringBuilder s = new StringBuilder();
		
		if (tokens==null || tokens.length==0)
		{
			return ViewEnum.getFullName(schema, mainSuffix);
		}
		
		for (Object token:tokens)
		{
			// if any token is null, the table name will be invalid, return null
			if (token==null)
			{
				return null;
			}
			s.append(token);
			s.append(Delimiters.SQL_TOKEN_DELIMITER);
		}
		s.append(mainSuffix);
		
		return ViewEnum.getFullNameNotNormalized(schema, s.toString());
		
	}
	
	public static String buildTableNameWithTokens(String schema, ViewEnum mainTableSuffix, Object... tokens)
	{
		return buildTableNameWithTokens(schema, mainTableSuffix.getTableName(), tokens);
	}
	
	public static String buildTableNameWithTokens(String schema, ColumnEnum mainTableSuffix, Object... tokens)
	{
		return buildTableNameWithTokens(schema, mainTableSuffix.getColumnName(), tokens);
	}
	
}
