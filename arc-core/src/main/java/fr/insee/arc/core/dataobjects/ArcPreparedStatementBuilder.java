package fr.insee.arc.core.dataobjects;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;

public class ArcPreparedStatementBuilder extends GenericPreparedStatementBuilder {

	public ArcPreparedStatementBuilder() {
		super();
	}

	public ArcPreparedStatementBuilder(String query) {
		super(query);
	}

	public ArcPreparedStatementBuilder(StringBuilder query) {
		super(query);
	}

	/**
	 * build a sql list of column based on a collection of arc column enumeration
	 * elements
	 * 
	 * @param listOfColumns
	 * @return
	 */
	public StringBuilder sqlListeOfColumnsFromModel(Collection<ColumnEnum> listOfColumns) {
		return sqlListeOfColumns(ColumnEnum.listColumnEnumByName(listOfColumns));
	}

	/**
	 * return a liste of column based on a variable array of arc column enumeration
	 * elements
	 * 
	 * @param columns
	 * @return
	 */
	public StringBuilder sqlListeOfColumnsFromModel(ColumnEnum... columns) {
		return sqlListeOfColumnsFromModel(Arrays.asList(columns));
	}

	/**
	 * return a liste of column based on a variable array of arc column enumeration
	 * elements
	 * 
	 * @param columns
	 * @return
	 */
	public StringBuilder sqlListeOfColumnsFromModel(Map<ColumnEnum, ColumnEnum> mapOfColumnEnum) {
		return sqlListeOfColumnsFromModel(mapOfColumnEnum.keySet());
	}

	/**
	 * return sql expression of table columns
	 * 
	 * @param columns
	 * @return
	 */
	public StringBuilder sqlListeOfColumnsFromModel(ViewEnum tableEnum) {
		return sqlListeOfColumnsFromModel(tableEnum.getColumns());
	}

	public ArcPreparedStatementBuilder append(ViewEnum view) {
		return (ArcPreparedStatementBuilder) this.append(view.getTableName());
	}
	
	public ArcPreparedStatementBuilder append(ColumnEnum column) {
		return (ArcPreparedStatementBuilder) this.append(column.getColumnName());
	}

	public String quoteText(ColumnEnum column) {
		return quoteText(column.getColumnName());
	}
	
	@Override
	public ArcPreparedStatementBuilder build(Object... queryElements) {
		for (Object queryElement : queryElements) {
			getQuery().append(queryElement);
		}
		return this;
	}
	
}
