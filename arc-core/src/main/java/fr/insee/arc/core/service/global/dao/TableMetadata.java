package fr.insee.arc.core.service.global.dao;

import java.util.ArrayList;
import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;

public class TableMetadata {

	private TableMetadata() {
		throw new IllegalStateException("Utility class");
	}

	public static ArcPreparedStatementBuilder queryTablesFromPgMetadata() {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.TABLE_SCHEMA, "||'.'||", ColumnEnum.TABLE_NAME, SQL.AS,
				ColumnEnum.TABLE_NAME);
		query.build(SQL.FROM, "information_schema.tables");
		return query;
	}


	/**
	 * Query to retrieve the table indexe ddl creation command
	 * @param tablename
	 * @return
	 * @throws ArcException 
	 */
	public static ArcPreparedStatementBuilder queryIndexesInformations(String tablename) throws ArcException
	{
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build("select indexdef from pg_indexes where schemaname||'.'||tablename = ", query.quoteText(tablename));
		return query;		
	}
	
	
	/**
	 * Query to rebuild a table eventually with its index
	 * Table will be copied in an img table, then dropped and img table will be renamed
	 * if indexes are provided, they will be rebuilt in the new table
	 * @param tableToRebuild
	 * @param indexInformations : a genericBean with a single column containing index ddl creation command
	 * @return
	 */
	public static ArcPreparedStatementBuilder rebuildTable(String tableToRebuild, GenericBean... indexInformations)
	{
		String tableImage = FormatSQL.imageObjectName(tableToRebuild);
		
		List<List<String>> indexInformation = indexInformations.length>0 ? indexInformations[0].getContent() : new ArrayList<>();

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build("DROP TABLE IF EXISTS ", tableImage," ", SQL.END_QUERY, SQL.NEW_LINE);
		
		query.build("CREATE TABLE ", tableImage, FormatSQL.WITH_NO_VACUUM, " AS SELECT * FROM ", tableToRebuild, SQL.END_QUERY, SQL.NEW_LINE );
		query.build("DROP TABLE IF EXISTS ",tableToRebuild, SQL.END_QUERY, SQL.NEW_LINE);
		query.build("ALTER TABLE ",tableImage, " RENAME TO ", FormatSQL.extractTableNameToken(tableToRebuild), SQL.END_QUERY, SQL.NEW_LINE);
		
		for (int i=0; i<indexInformation.size() ; i++)
		{
			query.build(indexInformation.get(i).get(0), SQL.END_QUERY, SQL.NEW_LINE);
		}
		
		query.build("ANALYZE ", tableToRebuild,  SQL.END_QUERY, SQL.NEW_LINE);

		return query;
		
	}
	
}
