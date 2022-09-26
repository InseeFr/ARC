package fr.insee.arc.core.databaseobjetcs;

public enum TypeEnum {

	TEXT("text",true)
	, TEXT_ARRAY("text[]", true)
	, INT("int", false)
	, BIGINT("bigint", false)
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
		return isCollated?(typeName+" "+DatabaseObjectService.DATABASE_COLLATION):typeName;
	}
	
}
