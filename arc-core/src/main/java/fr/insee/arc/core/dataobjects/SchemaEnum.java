package fr.insee.arc.core.dataobjects;

public enum SchemaEnum {

	METADATA // table in metadata schema (arc)
	, SANDBOX // table in a sandbox schema (arc_basX )
	, SANDBOX_GENERATED // table in a sandbox schema (arc_basX ) but built by application
	, TEMPORARY // temporary table in temporary schema
	, PUBLIC // table in public schema
	;

}
