package fr.insee.arc.utils.query;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

public class TestDatabase {
	
	public Connection testConnection;
	public Connection testConnectionExtra;

	public TestDatabase()
	{
	    EmbeddedPostgres pg;
	    try {
			pg = EmbeddedPostgres.start();
			testConnection = pg.getPostgresDatabase().getConnection();
			testConnectionExtra = pg.getPostgresDatabase().getConnection();

		} catch (IOException | SQLException e) {
			testConnectionExtra = null;
			testConnectionExtra = null;
		}
	}

	
}
