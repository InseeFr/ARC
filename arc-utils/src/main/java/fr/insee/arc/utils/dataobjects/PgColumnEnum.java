package fr.insee.arc.utils.dataobjects;

public enum PgColumnEnum {
	
	// columns for testing
	TEST1("test1", TypeEnum.TEXT, "test column 1"), TEST2("test2", TypeEnum.TEXT, "test column 2")
	
	, COLUMN_NAME("column_name", TypeEnum.TEXT, "column name in information schema")
	, TABLE_NAME("table_name", TypeEnum.TEXT, "table name in information schema")
	, TABLE_SCHEMA("table_schema", TypeEnum.TEXT, "schema name in information schema")
	
	, TABLENAME("tablename", TypeEnum.TEXT, "table name in pg_tables")
	, SCHEMANAME("schemaname", TypeEnum.TEXT, "schema name in pg_tables")

	;

	private String columnName;
	private TypeEnum columnType;
	private String columnExplanation;

	private PgColumnEnum(String columnName, TypeEnum columnType, String columnExplanation) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.columnExplanation = columnExplanation;
	}

	public String getColumnName() {
		return columnName;
	}

	public TypeEnum getColumnType() {
		return columnType;
	}

	public String getColumnExplanation() {
		return columnExplanation;
	}

	@Override
	public String toString() {
		return this.getColumnName();
	}

}
