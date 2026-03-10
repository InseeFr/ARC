package fr.insee.arc.utils.query;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

public class TestDatabase {

	public Connection testConnection = instanciateTestDatabase();

	private static Connection instanciateTestDatabase()
	{
	    EmbeddedPostgres pg;
	    Connection connection=null;
	    try {
			pg = EmbeddedPostgres.start();
			connection = pg.getPostgresDatabase().getConnection();

		} catch (IOException | SQLException e) {
			connection = null;
		}
	  return connection;
	}

	
}
