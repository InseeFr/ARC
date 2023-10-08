package fr.insee.arc.utils.dao;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.junit.Rule;
import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.LogAppenderResource;

public class UtilitaireDaoTest extends InitializeQueryTest {

	@Rule
	public LogAppenderResource appender = new LogAppenderResource(LogManager.getLogger(UtilitaireDao.class));
	
	@Test
	public void numberOfNods_test() {
		
		u.getProperties().setConnectionProperties(null);
		u.getProperties().setDatabaseUrl("{0=>\"db1\"},{1=>\"db2\"}");
		u.getProperties().setDatabaseUsername("{0=>\"db1\"},{1=>\"db2\"}");
		u.getProperties().setDatabasePassword("{0=>\"db1\"},{1=>\"db2\"}");
		u.getProperties().setDatabaseDriverClassName("{0=>\"db1\"},{1=>\"db2\"}");

		// must return 2 nods
		assertEquals(2,u.numberOfNods());
	}
	
	@Test
	public void getDriverConnexion_OK() throws SQLException, ArcException {
		
		PropertiesHandler testProperties=new PropertiesHandler();
		testProperties.setConnectionProperties(null);
		testProperties.setDatabaseDriverClassName("org.postgresql.Driver");
		testProperties.setDatabaseUrl(c.getMetaData().getURL());
		testProperties.setDatabaseUsername(c.getMetaData().getUserName());
		// user password is not relevant in zonky
		testProperties.setDatabasePassword("NA");
		
		u.setProperties(testProperties);

		Connection z=u.getDriverConnexion();
		
		int testQuery=u.getInt(z, new GenericPreparedStatementBuilder("select 1"));
		
		assertEquals(1, testQuery);
	}

	@Test(expected = ArcException.class)
	public void getDriverConnexion_KO() throws SQLException, ArcException {
		u.getProperties().setDatabaseDriverClassName("org.postgresql.Driver");
		u.getProperties().setDatabaseUrl(c.getMetaData().getURL());

		// user is fake. Connection will fail
		u.getProperties().setDatabaseUsername("fake_user");
		u.getProperties().setDatabasePassword("NA");
		
		u.getDriverConnexion();
	}

	
}
