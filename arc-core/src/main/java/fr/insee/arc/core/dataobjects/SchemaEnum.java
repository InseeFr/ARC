package fr.insee.arc.core.dataobjects;

import fr.insee.arc.utils.dataobjects.PgSchemaEnum;

public enum SchemaEnum {

	ARC_METADATA("arc") // table in metadata schema (arc)
	, SANDBOX ("arc_bas") // table in a sandbox schema (arc_basX )
	, SANDBOX_GENERATED ("arc_bas") // table in a sandbox schema (arc_basX ) but built by application
	, TEMPORARY (PgSchemaEnum.TEMPORARY) // temporary table in temporary schema
	, PUBLIC (PgSchemaEnum.PUBLIC)// table in public schema
	, CATALOG (PgSchemaEnum.CATALOG) // postgres catalog schema
	, INFORMATION_SCHEMA (PgSchemaEnum.INFORMATION_SCHEMA) // ANSI database metadata schema
	;

	private String schemaName;
	
	private SchemaEnum(String schemaName)
	{
		this.schemaName = schemaName;
	}
	
	private SchemaEnum(PgSchemaEnum pgSchema)
	{
		this.schemaName = pgSchema.getSchemaName();
	}

	public String getSchemaName() {
		return schemaName;
	}
	
	@Override
	public String toString() {
		return this.getSchemaName();
	}
}
