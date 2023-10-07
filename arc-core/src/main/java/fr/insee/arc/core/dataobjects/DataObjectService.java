package fr.insee.arc.core.dataobjects;

import org.springframework.stereotype.Component;

import fr.insee.arc.utils.dao.SQL;

@Component
public class DataObjectService {

	/**
	 * ARC special parameter
	 */
	public static final int MAX_NUMBER_OF_RECORD_PER_PARTITION = 100000;

	/**
	 * the sandbox schema
	 */
	private String sandboxSchema;

	/**
	 * Return the table name
	 * 
	 * @param e
	 * @return
	 */
	private String getViewRaw(ViewEnum e) {

		if ((e.getTableLocation().equals(SchemaEnum.SANDBOX)
				|| e.getTableLocation().equals(SchemaEnum.SANDBOX_GENERATED)) && this.sandboxSchema != null) {
			return this.sandboxSchema + SQL.DOT.getSqlCode() + e.getTableName();
		}

		return getFullTableNameInSchema(e.getTableLocation(), e.getTableName());

	}
	
	/**
	 * Return the table name (lowercase db convention)
	 * 
	 * @param e
	 * @return
	 */
	public String getView(ViewEnum e) {
		return getViewRaw(e).toLowerCase();
	}

	public static String getFullTableNameInSchema(SchemaEnum schema, String tablename) {
		return (schema.equals(SchemaEnum.TEMPORARY) ? tablename : schema.getSchemaName() + SQL.DOT.getSqlCode() + tablename).toLowerCase();
	}

	public String getSandboxSchema() {
		return sandboxSchema;
	}

	public void setSandboxSchema(String sandboxSchema) {
		this.sandboxSchema = sandboxSchema;
	}

}
