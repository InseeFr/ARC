package fr.insee.arc.core.service.global.dao;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.utils.dao.SQL;

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

}
