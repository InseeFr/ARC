package fr.insee.arc.core.dataobjects;

public class DataObjectService {
	
	/**
	 * the name of the poolname that jdbc connexions must use
	 */
	public static final String POOL_NAME_USED="arc";
	
	/**
	 * the proprietary application schema
	 */
	public static final String ARC_METADATA_SCHEMA="arc";
	
	
	/**
	 * database public schema
	 */
	public static final String PUBLIC_SCHEMA="public";

	
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

	public DataObjectService(String sandboxSchema) {
		super();
		this.sandboxSchema = sandboxSchema;
	}

	
	/**
	 * Return the SQL table name
	 * @param e
	 * @return
	 */
	public String getView(ViewEnum e)
	{
		switch (e.getTableLocation()) {
			case METADATA:
				return ARC_METADATA_SCHEMA+SCHEMA_SEPARATOR+e.getTableName();

			case SANDBOX:
				return this.sandboxSchema+SCHEMA_SEPARATOR+e.getTableName();
				
			case TEMPORARY:
				return e.getTableName();
				
			case PUBLIC:	
				return PUBLIC_SCHEMA+SCHEMA_SEPARATOR+e.getTableName();

		}
		return e.getTableName();		
	}
	
	public String getFullTableNameInMetadata(String tablename)
	{
		return ARC_METADATA_SCHEMA+"."+tablename;
	}
	
	
	public String getSandboxSchema() {
		return sandboxSchema;
	}

	public void setSandboxSchema(String sandboxSchema) {
		this.sandboxSchema = sandboxSchema;
	}
	
	

}
