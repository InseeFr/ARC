package fr.insee.arc.utils.dataobjects;

public enum TypeEnum {

	TEXT("text",true)
	, TEXT_ARRAY("text[]", true)
	, TIMESTAMP_ARRAY("timestamp[]", true)
	, INTEGER("int", false)
	, BIGINT("bigint", false)
	, NUMERIC("numeric",false)
	, SERIAL("serial",false)
	, DATE("date",false)
	, NAME("name",false)
	, TIMESTAMP("timestamp",false)
	, BOOLEAN("boolean",false)
	;
	

	/**
	 * database collation
	 */
	public static final String DATABASE_COLLATION="collate \"C\"";
	
	
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
		return isCollated?(typeName+" "+DATABASE_COLLATION):typeName;
	}
	
}
