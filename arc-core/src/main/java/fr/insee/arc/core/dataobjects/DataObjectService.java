package fr.insee.arc.core.dataobjects;

import fr.insee.arc.utils.dao.SQL;

public class DataObjectService {

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

		if ((e.getTableLocation().equals(SchemaEnum.SANDBOX)
				|| e.getTableLocation().equals(SchemaEnum.SANDBOX_GENERATED)) && this.sandboxSchema != null) {
			return this.sandboxSchema + SQL.DOT.getSqlCode() + e.getTableName();
		}

		return getFullTableNameInSchema(e.getTableLocation(), e.getTableName());

	}

	public static String getFullTableNameInSchema(SchemaEnum schema, String tablename) {
		return schema.equals(SchemaEnum.TEMPORARY) ? tablename : schema.getSchemaName() + SQL.DOT.getSqlCode() + tablename;
	}

	public String getSandboxSchema() {
		return sandboxSchema;
	}

	public void setSandboxSchema(String sandboxSchema) {
		this.sandboxSchema = sandboxSchema;
	}

}
