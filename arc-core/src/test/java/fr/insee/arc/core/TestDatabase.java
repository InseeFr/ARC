package fr.insee.arc.core;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

public class TestDatabase {

	public static Connection testConnection = instanciateDatabase();

	private static Connection instanciateDatabase()
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
