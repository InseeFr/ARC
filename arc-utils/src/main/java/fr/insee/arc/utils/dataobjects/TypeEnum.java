package fr.insee.arc.utils.dataobjects;

public enum TypeEnum {

	TEXT("text", "S", true)
	, TEXT_ARRAY("text[]", "S", true)
	, TIMESTAMP_ARRAY("timestamp[]", "D", true)
	, INTEGER("int", "int4", "N", false)
	, BIGINT("bigint", "int8", "N", false)
	, SMALLINT("smallint", "int2", "N", false)
	, REAL("real", "float4", "N", false)
	, DOUBLE("double", "float8", "N", false)
	, NUMERIC("numeric", "decimal", "N", false)
	, SERIAL("serial", "serial4", "N", false)
	, DATE("date", "D", false)
	, NAME("name", "S", false)
	, TIME("time", "D", false)
	, TIMETZ("timetz", "D", false)
	, TIMESTAMP("timestamp", "D", false)
	, TIMESTAMPTZ("timestamptz", "D", false)
	, BOOLEAN("boolean", "bool", "B", false)
	;
	

	/**
	 * database collation
	 */
	public static final String DATABASE_COLLATION="collate \"C\"";
	
	
	private TypeEnum(String typeName, String realName, String category, boolean isCollated) {
		this.typeName = typeName;
		this.realName = realName;
		this.category = category;
		this.isCollated = isCollated;
	}
	
	private TypeEnum(String typeName, String category, boolean isCollated) {
		this(typeName, typeName, category, isCollated);
	}

	private String typeName;
	
	private String realName;
	
	private String category;
	
	private boolean isCollated;

	public String getTypeName() {
		return typeName;
	}
	
	public String getRealName() {
		return realName;
	}
	
	public static TypeEnum realNameOf(String realName) {
		for (TypeEnum type : values()) {
	        if (type.realName.equals(realName)) {
	            return type;
	        }
	    }    
	    throw new IllegalArgumentException(realName);
	}

	public boolean isString() {
		return category.equals("S");
	}

	public boolean isNumeric() {
		return category.equals("N");
	}

	public boolean isDate() {
		return category.equals("D");
	}

	public boolean isBoolean() {
		return category.equals("B");
	}

	public boolean isCollated() {
		return isCollated;
	}

	public String getTypeCollated()
	{
		return isCollated?(typeName+" "+DATABASE_COLLATION):typeName;
	}
	
}
