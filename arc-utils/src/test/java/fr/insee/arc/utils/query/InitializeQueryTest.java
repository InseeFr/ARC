package fr.insee.arc.utils.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;

public class InitializeQueryTest {


	public static UtilitaireDao u = UtilitaireDao.get(0);
    
    public static Connection c = new TestDatabase().testConnection;

    public static Connection e;


    @Test
    public void testConnection()
    {
    	assertNotNull(c);
    }
    
    protected static void buildPropertiesWithoutScalability(String repertoire) throws SQLException
	{
    	buildProperties(repertoire, new Connection[] {c});	
	}
    
    
    protected static void buildPropertiesWithScalability(String repertoire) throws SQLException
	{
		e = new TestDatabase().testConnection;
    	buildProperties(repertoire, new Connection[] {c, e});	
	}
    
    protected static void buildProperties(String repertoire, Connection[] connections) throws SQLException
	{
		PropertiesHandler testProperties=PropertiesHandler.getInstance();
		
		boolean first=true;
		StringBuilder url=new StringBuilder();
		StringBuilder username=new StringBuilder();
		StringBuilder password=new StringBuilder();
		StringBuilder driver=new StringBuilder();

		for (Connection singleConnection:connections)
		{
			if (first)
			{
				first=false;
			}
			else
			{
				url.append(UtilitaireDao.CONNECTION_SEPARATOR_RAW);
				username.append(UtilitaireDao.CONNECTION_SEPARATOR_RAW);
				password.append(UtilitaireDao.CONNECTION_SEPARATOR_RAW);
				driver.append(UtilitaireDao.CONNECTION_SEPARATOR_RAW);
			}
			url.append(singleConnection.getMetaData().getURL());
			username.append(singleConnection.getMetaData().getUserName());
			// user password is not relevant in zonky
			password.append("NA");
			driver.append("org.postgresql.Driver");
		}
		
		testProperties.setDatabaseUrl(url.toString());
		testProperties.setDatabaseUsername(username.toString());
		testProperties.setDatabasePassword(password.toString());
		testProperties.setDatabaseDriverClassName(driver.toString());

		testProperties.setBatchParametersDirectory(repertoire);
		
		u.setProperties(testProperties);		
	}

	
	/**
	 * check the table columns and the number of lines in the table
	 * @param tableOut
	 * @throws ArcException
	 */
    protected static void testMetadataAndNumberOfRecords(String tableOut, int numberOfRecordsInTableOut, String[] columns) throws ArcException
	{
		
		// query the content in tableOut
		HashMap<String, ArrayList<String>> content = new GenericBean(
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
		HashMap<String, ArrayList<String>> content;
		
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
