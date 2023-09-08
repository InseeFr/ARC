package fr.insee.arc.utils.dataobjects;

import java.util.LinkedHashMap;
import java.util.Map;

public enum PgViewEnum {

	// tables utilisés pour les tests

	TABLE_TEST_IN_PUBLIC("table_test_in_public",PgSchemaEnum.PUBLIC,PgColumnEnum.TEST1,PgColumnEnum.TEST2)
	, TABLE_TEST_OUT_PUBLIC("table_test_out_public",PgSchemaEnum.PUBLIC,PgColumnEnum.TEST1,PgColumnEnum.TEST2)
	
	, TABLE_TEST_IN_TEMPORARY("table_test_in_temp",PgSchemaEnum.TEMPORARY,PgColumnEnum.TEST1,PgColumnEnum.TEST2)
	, TABLE_TEST_OUT_TEMPORARY("table_test_out_temp",PgSchemaEnum.TEMPORARY,PgColumnEnum.TEST1,PgColumnEnum.TEST2)

	
	// view for table aliases or temporary table in query
	, T1("t1",PgSchemaEnum.TEMPORARY), T2("t2",PgSchemaEnum.TEMPORARY), T3("t3",PgSchemaEnum.TEMPORARY)
	
	// postgres meta table
	, PG_TABLES("pg_tables", PgSchemaEnum.TEMPORARY, PgColumnEnum.SCHEMANAME, PgColumnEnum.TABLENAME)
	;

	private PgViewEnum(String tableName, PgSchemaEnum location, PgColumnEnum...columns) {
		this.tableName = tableName;
		this.tableLocation = location;
		
		this.columns=new LinkedHashMap<>();
		for (PgColumnEnum col:columns)
		{
			this.columns.put(col, col);
			
		}
		
	}

	/**
	 * database real name
	 */
	private String tableName;

	/**
	 * indicate if the table belongs to a sandbox
	 */
	private PgSchemaEnum tableLocation;

	private Map<PgColumnEnum,PgColumnEnum> columns;

	public String getTableName() {
		return tableName;
	}
	
	public PgSchemaEnum getTableLocation() {
		return tableLocation;
	}

	public Map<PgColumnEnum,PgColumnEnum> getColumns() {
		return columns;
	}
	
}
