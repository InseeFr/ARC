package fr.insee.arc.core.service.global.bo;

import java.sql.Connection;

public class Sandbox {

	private Connection connection;
	
	private String schema;

	
	/**
	 * instanciate a sandbox reference for execution
	 * @param connection
	 * @param schema
	 */
	public Sandbox(Connection connection, String schema) {
		super();
		this.connection = connection;
		this.schema = schema;
	}

	public Connection getConnection() {
		return connection;
	}

	public String getSchema() {
		return schema;
	}

}
