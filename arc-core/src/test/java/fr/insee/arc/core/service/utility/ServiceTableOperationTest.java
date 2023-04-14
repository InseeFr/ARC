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
    
    static int defaultExpectedNumberOfRecordsForTest = 5; 
    static String[] defaultExpectedColumnsForTest = new String[] {"i", "j"};
    
	@Test
	/**
	 * test the query creating a table image of another table
	 * @throws ArcException
	 */
	public void creationTableResultatTestSchemaTables() throws ArcException {

		String tableIn = "public.table_test_in";
		String tableOut = "public.table_test_out";
		
		// create a table with 5 records and 2 columns (i,j)
		u.executeImmediate(c, "CREATE TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+defaultExpectedNumberOfRecordsForTest+") i");
		
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
		testCreationTableResultatResult(tableOut, defaultExpectedNumberOfRecordsForTest);
		
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
		u.executeImmediate(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+defaultExpectedNumberOfRecordsForTest+") i");
		
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
		testCreationTableResultatResult(tableOut, defaultExpectedNumberOfRecordsForTest);
		
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
		u.executeImmediate(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+defaultExpectedNumberOfRecordsForTest+") i");

		// execute createTableInherit to create the table duplication
		u.executeImmediate(c, ServiceTableOperation.createTableInherit(tableIn, tableOut));
		
		// test
		// tableOut must exists
		testTableExists(tableOut,1);
		// the data must be same as tableIn
		testCreationTableResultatResult(tableOut,defaultExpectedNumberOfRecordsForTest);
		u.dropTable(c, tableIn, tableOut);
	
		
		// test 2: tableIn is empty
		u.executeImmediate(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+defaultExpectedNumberOfRecordsForTest+") i where false");
		// execute createTableInherit to create the table duplication
		u.executeImmediate(c, ServiceTableOperation.createTableInherit(tableIn, tableOut));

		// test
		// tableOut shouldn't have been created and doesn't exist
		testTableExists(tableOut,0);
		u.dropTable(c, tableIn, tableOut);
		
	}

	@Test
	public void createTableTravailIdSourceTest() throws ArcException
	{
		String idSource="mon_fichier.txt";
		String tableIn="public.chargement_ok";
		String tableOut="table_temporaire_data";
		
		String tableOfIdSource=ServiceHashFileName.tableOfIdSource(tableIn, idSource);

		
		// creation de la table de données relative au fichier
		u.executeImmediate(c, "CREATE TABLE "+tableOfIdSource+" as SELECT i, i+1 as j FROM generate_series(1,"+defaultExpectedNumberOfRecordsForTest+") i");

		// creation de la table temporaire de données relative copie de la table de données du fichier
		u.executeImmediate(c, ServiceTableOperation.createTableTravailIdSource(tableIn, tableOut, idSource));
				
		// test if content is the same
		testCreationTableResultatResult(tableOut, defaultExpectedNumberOfRecordsForTest);
		u.dropTable(c, tableOfIdSource, tableOut);

		
		// testing with extra columns definition
		// creation de la table de données relative au fichier
		u.executeImmediate(c, "CREATE TABLE "+tableOfIdSource+" as SELECT i, i+1 as j FROM generate_series(1,"+defaultExpectedNumberOfRecordsForTest+") i");

		// creation de la table temporaire de données relative copie de la table de données du fichier
		u.executeImmediate(c, ServiceTableOperation.createTableTravailIdSource(tableIn, tableOut, idSource, "null::text as k, 8::int as l"));
		
		String[] expectedColumns = new String[] {"i", "j", "k", "l"};
		testCreationTableResultatResult(tableOut, defaultExpectedNumberOfRecordsForTest, expectedColumns);
		
		u.dropTable(c, tableOfIdSource, tableOut);

	}
	
	
	/**
	 * check the table columns and the number of lines in the table
	 * @param tableOut
	 * @throws ArcException
	 */
	private void testCreationTableResultatResult(String tableOut, int numberOfRecordsInTableOut, String... columns) throws ArcException
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
	
	private void testCreationTableResultatResult(String tableOut, int numberOfRecordsInTableOut) throws ArcException
	{
		String[] columns = defaultExpectedColumnsForTest;
		
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
