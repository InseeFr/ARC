package fr.insee.arc.core.databaseobjects;

public class DatabaseObjectService {
	
	/**
	 * the name of the poolname that jdbc connexions must use
	 */
	public static final String POOL_NAME_USED="arc";
	
	/**
	 * the proprietary application schema
	 */
	public static final String ARC_METADATA_SCHEMA="arc";
	
	
	/**
	 * database collation
	 */
	public static final String DATABASE_COLLATION="collate \"C\"";
	
	/**
	 * database characters convention
	 */
	public static final String SCHEMA_SEPARATOR=".";
	public static final String QUERY_SEPARATOR=";";
	
	
	/**
	 * ARC special parameter
	 */
	public static final int MAX_NUMBER_OF_RECORD_PER_PARTITION=100000;
	
	
	
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
		return e.isTableInSanbox()?this.sandboxSchema+SCHEMA_SEPARATOR+e.getTableName():ARC_METADATA_SCHEMA+SCHEMA_SEPARATOR+e.getTableName();
	}
	

	
	
	public String getSandboxSchema() {
		return sandboxSchema;
	}

	public void setSandboxSchema(String sandboxSchema) {
		this.sandboxSchema = sandboxSchema;
	}
	
	

}
