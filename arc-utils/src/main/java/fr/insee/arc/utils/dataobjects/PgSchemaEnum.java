package fr.insee.arc.utils.dataobjects;

public enum PgSchemaEnum {
	TEMPORARY ("") // temporary table in temporary schema
	, PUBLIC ("public")// table in public schema
	, CATALOG ("pg_catalog") // postgres catalog schema
	, INFORMATION_SCHEMA ("information_schema") // ANSI database metadata schema
	;

	private String schemaName;
	
	private PgSchemaEnum(String schemaName)
	{
		this.schemaName = schemaName;
	}

	public String getSchemaName() {
		return schemaName;
	}
	
	@Override
	public String toString() {
		return this.getSchemaName();
	}
}

