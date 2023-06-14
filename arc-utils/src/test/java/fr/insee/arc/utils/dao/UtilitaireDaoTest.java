package fr.insee.arc.utils.dao;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.junit.Rule;
import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.utils.LogAppenderResource;

public class UtilitaireDaoTest extends InitializeQueryTest {

	@Rule
	public LogAppenderResource appender = new LogAppenderResource(LogManager.getLogger(UtilitaireDao.class));
	
	@Test
	public void computeNumberOfExecutorNods_test() {
		// settings 4 database
		// 1 is meta data
		// 2 is coordinator
		// 3+ are executor
		u.getProperties().setDatabaseUsername("arc|||arc|||arc|||arc");
		assertEquals(3,u.computeNumberOfExecutorNods());
	}
	
	@Test
	public void getDriverConnexion_OK() throws SQLException, ArcException {
		u.getProperties().setDatabaseDriverClassName("org.postgresql.Driver");
		u.getProperties().setDatabaseUrl(c.getMetaData().getURL());
		u.getProperties().setDatabaseUsername(c.getMetaData().getUserName());
		// user password is not relevant in zonky
		u.getProperties().setDatabasePassword("NA");
		
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
