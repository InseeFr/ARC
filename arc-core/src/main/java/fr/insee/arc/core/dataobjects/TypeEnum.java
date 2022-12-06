package fr.insee.arc.core.dataobjects;

public enum TypeEnum {

	TEXT("text",true)
	, TEXT_ARRAY("text[]", true)
	, INT("int", false)
	, BIGINT("bigint", false)
	, SERIAL("serial",false)
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
