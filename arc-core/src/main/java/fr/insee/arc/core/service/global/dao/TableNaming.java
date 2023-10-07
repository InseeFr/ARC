package fr.insee.arc.core.service.global.dao;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.utils.dao.SQL;
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
	public static String temporaryTableName(String aEnvExecution, String aCurrentPhase, ViewEnum table) {
		return ViewEnum.getFullName(aEnvExecution, FormatSQL.temporaryTableName(aCurrentPhase + "_" + table));
	}

	public static String phaseDataTableName(String aEnvExecution, String aPhase, TraitementEtat etat) {
		return ViewEnum.getFullName(aEnvExecution, aPhase + "_" + etat.toString());
	}
    
    public static ArcPreparedStatementBuilder queryTablesFromPgMetadata()
    {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.TABLE_SCHEMA, "||'.'||", ColumnEnum.TABLE_NAME, SQL.AS, ColumnEnum.TABLE_NAME);
		query.build(SQL.FROM, "information_schema.tables");
		return query;
    }
	
}
