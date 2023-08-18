package fr.insee.arc.core.dataobjects;

public class DataObjectService {

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

	public DataObjectService() {
		super();
	}

	
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
		if (e.getTableLocation().equals(SchemaEnum.METADATA))
		{
			return getFullTableNameInMetadata(e.getTableName());
		}
		
		if (e.getTableLocation().equals(SchemaEnum.TEMPORARY))
		{
			return e.getTableName();
		}
		
		if (e.getTableLocation().equals(SchemaEnum.PUBLIC))
		{
			return PUBLIC_SCHEMA+SCHEMA_SEPARATOR+e.getTableName();
		}

		if ((e.getTableLocation().equals(SchemaEnum.SANDBOX) || e.getTableLocation().equals(SchemaEnum.SANDBOX_GENERATED)) && this.sandboxSchema != null )
		{
			return this.sandboxSchema+SCHEMA_SEPARATOR+e.getTableName();
		}
		
		return e.getTableName();		
	}
	
	public static String getFullTableNameInMetadata(String tablename)
	{
		return ARC_METADATA_SCHEMA + SCHEMA_SEPARATOR + tablename;
	}
	
	
	public String getSandboxSchema() {
		return sandboxSchema;
	}

	public void setSandboxSchema(String sandboxSchema) {
		this.sandboxSchema = sandboxSchema;
	}
	
	

}
