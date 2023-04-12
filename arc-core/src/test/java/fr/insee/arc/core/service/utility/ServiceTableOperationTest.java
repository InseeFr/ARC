package fr.insee.arc.core.service.utility;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import fr.insee.arc.core.TestDatabase;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;


public class ServiceTableOperationTest {

    UtilitaireDao u = new UtilitaireDao();
    
    Connection c = TestDatabase.testConnection;
    
    static int numberOfRecordsForTest = 5; 
	
	@Test
	/**
	 * test the query creating a table image of another table
	 * @throws ArcException
	 */
	public void creationTableResultatTestSchemaTables() throws ArcException {

		String tableIn = "public.table_test_in";
		String tableOut = "public.table_test_out";
		
		// create a table with 5 records and 2 columns (i,j)
		u.executeImmediate(c, "CREATE TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+numberOfRecordsForTest+") i");
		
		// create tableOut as an empty image of tableIn
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut));
		testCreationTableResultatResult(tableOut, 0);
		u.dropTable(c, tableOut);
		
		// create tableOut as an empty image of tableIn (false argument)
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut, false));
		testCreationTableResultatResult(tableOut, 0);
		u.dropTable(c, tableOut);

		// create tableOut as the exact image of tableIn (true argument)
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut, true));
		testCreationTableResultatResult(tableOut, numberOfRecordsForTest);
		
		u.dropTable(c, tableIn, tableOut);
	}
	
	@Test
	/**
	 * test the query creating a temporary table image of another table
	 * @throws ArcException
	 */
	public void creationTableResultatTestTemporaryTables() throws ArcException {

		String tableIn = "table_test_in";
		String tableOut = "table_test_out";
		
		// create a table with 5 records and 2 columns (i,j)
		u.executeImmediate(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+numberOfRecordsForTest+") i");
		
		// create tableOut as an empty image of tableIn
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut));
		testCreationTableResultatResult(tableOut, 0);
		u.dropTable(c, tableOut);
		
		// create tableOut as an empty image of tableIn (false argument)
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut, false));
		testCreationTableResultatResult(tableOut, 0);
		u.dropTable(c, tableOut);

		// create tableOut as the exact image of tableIn (true argument)
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut, true));
		testCreationTableResultatResult(tableOut, numberOfRecordsForTest);
		
		u.dropTable(c, tableIn, tableOut);
		
	}

	
	@Test
	/**
	 * Test the query creating the image of another table under records exist condition
	 * The query creates an image if and only if there are some records in the source table
	 * @throws ArcException 
	 */
	public void createTableInheritTest() throws ArcException
	{
		
		String tableIn = "table_test_in";
		String tableOut = "public.table_test_out";
		
		// test 1: tableIn is not empty
		// create a table with 5 records and 2 columns (i,j)
		u.executeImmediate(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+numberOfRecordsForTest+") i");

		// execute createTableInherit to create the table duplication
		u.executeImmediate(c, ServiceTableOperation.createTableInherit(tableIn, tableOut));
		
		// test
		// tableOut must exists
		testTableExists(tableOut,1);
		// the data must be same as tableIn
		testCreationTableResultatResult(tableOut,numberOfRecordsForTest);
		u.dropTable(c, tableIn, tableOut);
	
		
		// test 2: tableIn is empty
		u.executeImmediate(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+numberOfRecordsForTest+") i where false");
		// execute createTableInherit to create the table duplication
		u.executeImmediate(c, ServiceTableOperation.createTableInherit(tableIn, tableOut));

		// test
		// tableOut shouldn't have been created and doesn't exist
		testTableExists(tableOut,0);
		u.dropTable(c, tableIn, tableOut);
		
	}

	
	
	/**
	 * check the table columns and the number of lines in the table
	 * @param tableOut
	 * @throws ArcException
	 */
	private void testCreationTableResultatResult(String tableOut, int numberOfRecordsInTableOut) throws ArcException
	{
		
		// query the content in tableOut
		HashMap<String, ArrayList<String>> content = new GenericBean(
				    u.executeRequest(c, new GenericPreparedStatementBuilder("SELECT * FROM "+tableOut))).mapContent(true);

		// test that there is exactly 2 columns in tableOut
		assertEquals(2, content.keySet().size());
		
		// test that tableOut has a 2 columns called i and j
		// and that tableOut is empty
		assertEquals(numberOfRecordsInTableOut, content.get("i").size());
		assertEquals(numberOfRecordsInTableOut, content.get("j").size());
	}
	
	private void testTableExists(String tableOut, int expectedNumber) throws ArcException
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
