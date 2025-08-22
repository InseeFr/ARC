package fr.insee.arc.utils.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
		testProperties.setDatabaseDriverClassName("org.postgresql.Driver");
		testProperties.setDatabaseUrl(c.getMetaData().getURL());
		testProperties.setDatabaseUsername(c.getMetaData().getUserName());
		// user password is not relevant in zonky
		testProperties.setDatabasePassword("NA");
		
		u.setProperties(testProperties);

		Connection z=u.getDriverConnexion();
		
		int testQuery=u.getInt(z, new GenericPreparedStatementBuilder("select 1"));
		
		assertEquals(1, testQuery);
		
		// test retrieveConnectionAttribute
		assertEquals("NA", testProperties.retrieveConnectionAttribute(z).getDatabasePassword());
		
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

	@Test
	public void outStreamRequeteSelect() throws ArcException {
		UtilitaireDao.get(0).executeRequest(c, "DROP SCHEMA IF EXISTS test CASCADE;");
		UtilitaireDao.get(0).executeRequest(c, "CREATE SCHEMA test;");
		
		// create a test table with 26 lines
		UtilitaireDao.get(0).executeRequest(c, "CREATE TABLE test.table_test as select i as id, chr(i+64) as val, array[i,i+1] as arr, current_date as dd from generate_series(1,26) i;");
		
		// test method
		GenericPreparedStatementBuilder requete = new GenericPreparedStatementBuilder("SELECT * FROM test.table_test");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		u.outStreamRequeteSelect(c, requete, bos);
		
		// assert
		byte[] outputArray = bos.toByteArray();
		// export must contain 1 line for columns name + 1 line for columns type + 26 lines of data
		assertEquals(28, StringUtils.countMatches(new String(outputArray), "\n"));
		UtilitaireDao.get(0).executeRequest(c, "DROP SCHEMA IF EXISTS test CASCADE;");
	}
	
	@Test
	public void getColumnsTest() throws ArcException
	{
		
		u.executeRequest(c, new GenericPreparedStatementBuilder("DISCARD TEMP;"));
		u.executeRequest(c, new GenericPreparedStatementBuilder("CREATE TEMPORARY TABLE tmp_a (a1 text, a2 int);"));
		u.executeRequest(c, new GenericPreparedStatementBuilder("CREATE TEMPORARY TABLE tmp_b (b1 text, b2 date);"));
		
		List<String> cols = new ArrayList<String>();
		
		u.getColumns(c, cols, "tmp_a");
		u.getColumns(c, cols, "tmp_b");
		
		assertTrue(cols.contains("a1"));
		assertTrue(cols.contains("a2"));
		assertTrue(cols.contains("b1"));
		assertTrue(cols.contains("b2"));
		assertEquals(4, cols.size());
		
		u.executeRequest(c, "DISCARD TEMP;");
	}
	
	
	
}
