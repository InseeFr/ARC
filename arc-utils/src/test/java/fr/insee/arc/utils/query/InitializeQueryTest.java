package fr.insee.arc.utils.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class InitializeQueryTest {


    private static UtilitaireDao u = UtilitaireDao.get(0);
    
    protected static Connection c = TestDatabase.testConnection;
    

    @Test
    public void testConnection()
    {
    	assertNotNull(c);
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
	
	protected void testTableExists(String tableOut, int expectedNumber) throws ArcException
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
