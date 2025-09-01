package fr.insee.arc.core.dataobjects;

import org.springframework.stereotype.Component;

import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.database.Delimiters;

@Component
public class DataObjectService {

	/**
	 * ARC special parameter
	 */
	public static final int MAX_NUMBER_OF_RECORD_PER_PARTITION = 100000;

	public static final String APPLICATION_NAME = "ARC";
	
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
			return this.sandboxSchema + Delimiters.SQL_SCHEMA_DELIMITER + e.getTableName();
		}

		return ViewEnum.getFullTableNameInSchema(e.getTableLocation(), e.getTableName());

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

	public String getSandboxSchema() {
		return sandboxSchema;
	}

	public void setSandboxSchema(String sandboxSchema) {
		this.sandboxSchema = sandboxSchema;
	}

}
