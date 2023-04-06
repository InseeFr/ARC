package fr.insee.arc.core.dataobjects;

public enum TypeEnum {

	TEXT("text",true)
	, TEXT_ARRAY("text[]", true)
	, INTEGER("int", false)
	, BIGINT("bigint", false)
	, NUMERIC("numeric",false)
	, SERIAL("serial",false)
	, DATE("date",false)
	, NAME("name",false)
	, TIMESTAMP("timestamp",false)
	, BOOLEAN("boolean",false)
	;
	
	private TypeEnum(String typeName, boolean isCollated) {
		this.typeName = typeName;
		this.isCollated = isCollated;
	}

	private String typeName;
	
	private boolean isCollated;

	public String getTypeName() {
		return typeName;
	}


	public boolean isCollated() {
		return isCollated;
	}

	public String getTypeCollated()
	{
		return isCollated?(typeName+" "+DataObjectService.DATABASE_COLLATION):typeName;
	}
	
}
