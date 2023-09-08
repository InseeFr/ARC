package fr.insee.arc.core.dataobjects;

public class DataObjectService {

	/**
	 * database characters convention
	 */
	public static final String SCHEMA_SEPARATOR = ".";
	public static final String QUERY_SEPARATOR = ";";

	/**
	 * ARC special parameter
	 */
	public static final int MAX_NUMBER_OF_RECORD_PER_PARTITION = 100000;

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
	 * 
	 * @param e
	 * @return
	 */
	public String getView(ViewEnum e) {

		if (e.getTableLocation().equals(SchemaEnum.TEMPORARY)) {
			return e.getTableName();
		}
		if ((e.getTableLocation().equals(SchemaEnum.SANDBOX)
				|| e.getTableLocation().equals(SchemaEnum.SANDBOX_GENERATED)) && this.sandboxSchema != null) {
			return this.sandboxSchema + SCHEMA_SEPARATOR + e.getTableName();
		}

		return getFullTableNameInSchema(e.getTableLocation(), e.getTableName());

	}

	public static String getFullTableNameInSchema(SchemaEnum schema, String tablename) {
		return schema.getSchemaName().equals("") ? tablename : schema.getSchemaName() + SCHEMA_SEPARATOR + tablename;
	}

	public String getSandboxSchema() {
		return sandboxSchema;
	}

	public void setSandboxSchema(String sandboxSchema) {
		this.sandboxSchema = sandboxSchema;
	}

}
