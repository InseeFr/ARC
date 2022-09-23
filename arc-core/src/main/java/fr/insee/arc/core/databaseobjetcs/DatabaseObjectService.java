package fr.insee.arc.core.databaseobjetcs;

public class DatabaseObjectService {
	
	/**
	 * the name of the poolname that jdbc connexions must use
	 */
	public static final String POOL_NAME_USED="arc";
	
	/**
	 * the proprietary application schema
	 */
	public static final String ARC_SCHEMA="arc";
	
	
	/**
	 * database characters convention
	 */
	public static final String SCHEMA_SEPARATOR=".";
	public static final String QUERY_SEPARATOR=";";
	
	/**
	 * the sandbox schema
	 */
	private String sandboxSchema;

	public DatabaseObjectService(String sandboxSchema) {
		super();
		this.sandboxSchema = sandboxSchema;
	}

	
	
	public String getTable(TableEnum e)
	{
		return e.isTableInSanbox()?this.sandboxSchema+SCHEMA_SEPARATOR+e.getTableName():ARC_SCHEMA+SCHEMA_SEPARATOR+e.getTableName();
	}
	

	
	
	public String getSandboxSchema() {
		return sandboxSchema;
	}

	public void setSandboxSchema(String sandboxSchema) {
		this.sandboxSchema = sandboxSchema;
	}
	
	

}
