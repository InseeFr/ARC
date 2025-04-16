package fr.insee.arc.utils.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;

public class InitializeQueryTest {


	public static UtilitaireDao u = UtilitaireDao.get(0);
    
    public static Connection c = new TestDatabase().testConnection;

    public static Connection e1;

    public static Connection e2;


    @Test
    public void testConnection()
    {
    	assertNotNull(c);
    }
    
    public static void buildPropertiesWithoutScalability(String repertoire) throws SQLException
	{
    	buildProperties(repertoire, new Connection[] {c});	
	}
    
    
    public static void buildPropertiesWithOneExecutor(String repertoire) throws SQLException
	{
		e1 = new TestDatabase().testConnection;
    	buildProperties(repertoire, new Connection[] {c, e1});	
	}
    
    public static void buildPropertiesWithTwoExecutors(String repertoire) throws SQLException
	{
		e1 = new TestDatabase().testConnection;
		e2 = new TestDatabase().testConnection;
    	buildProperties(repertoire, new Connection[] {c, e1, e2});	
	}
    
    
    private static void buildProperties(String repertoire, Connection[] connections) throws SQLException
	{
		PropertiesHandler testProperties=PropertiesHandler.getInstance();
		
		// reset connection properties
		StringBuilder url=new StringBuilder();
		StringBuilder username=new StringBuilder();
		StringBuilder password=new StringBuilder();
		StringBuilder driver=new StringBuilder();

		int index=0;
		for (Connection singleConnection:connections)
		{
			url.append(buildRuby(index,singleConnection.getMetaData().getURL()));
			username.append(buildRuby(index,singleConnection.getMetaData().getUserName()));
			// user password is not relevant in zonky
			password.append(buildRuby(index,"NA"));
			driver.append(buildRuby(index,"org.postgresql.Driver"));
			index ++;
		}
		
		url.setLength(url.length()-1);
		username.setLength(username.length()-1);
		password.setLength(password.length()-1);
		driver.setLength(driver.length()-1);
		
		testProperties.setDatabaseUrl(url.toString());
		testProperties.setDatabaseUsername(username.toString());
		testProperties.setDatabasePassword(password.toString());
		testProperties.setDatabaseDriverClassName(driver.toString());
		
		testProperties.setBatchParametersDirectory(repertoire);
		// disable s3 endpoint for internal test
		testProperties.setS3InputApiUri("");
		
		u.setProperties(testProperties);		
	}

    
    private static String buildRuby(int index, String inputString)
    {
    	return "{"+index+"=>\""+inputString+"\"},";
    }
	
	/**
	 * check the table columns and the number of lines in the table
	 * @param tableOut
	 * @throws ArcException
	 */
    protected static void testMetadataAndNumberOfRecords(String tableOut, int numberOfRecordsInTableOut, String[] columns) throws ArcException
	{
		
		// query the content in tableOut
		Map<String, List<String>> content = new GenericBean(
				    u.executeRequest(c, new GenericPreparedStatementBuilder("SELECT * FROM "+tableOut))).mapContent(true);

		// test that there is exactly 2 columns in tableOut
		assertEquals(columns.length, content.keySet().size());
		
		// test that tableOut has a 2 columns called i and j
		// and that tableOut is empty
		for (int columnIndex=0; columnIndex<columns.length; columnIndex++)
		{
			assertEquals(numberOfRecordsInTableOut, content.get(columns[columnIndex]).size());
		}
	}
	
    protected static void testTableExists(String tableOut, int expectedNumber) throws ArcException
	{
		Map<String, List<String>> content;
		
		if (tableOut.contains("."))
		{
		content= new GenericBean(
			    u.executeRequest(c, new GenericPreparedStatementBuilder("SELECT count(*) as number_of_table FROM pg_tables where schemaname||'.'||tablename='"+tableOut+"'"))).mapContent(true);
		}
		else
		{
			content= new GenericBean(
				    u.executeRequest(c, new GenericPreparedStatementBuilder("SELECT count(*) as number_of_table FROM pg_tables where tablename='"+tableOut+"'"))).mapContent(true);
		}
		
		assertEquals(expectedNumber, Integer.parseInt(content.get("number_of_table").get(0)));

	}
	
}
