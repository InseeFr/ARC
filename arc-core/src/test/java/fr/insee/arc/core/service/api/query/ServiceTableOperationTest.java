package fr.insee.arc.core.service.api.query;

import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;


public class ServiceTableOperationTest extends InitializeQueryTest {
   
	int expectedNumberOfRecordsForTest = 5;
	String[] expectedColumnsForTest = new String[] {"i", "j"};

	@Test
	/**
	 * test the query creating a table image of another table
	 * @throws ArcException
	 */
	public void creationTableResultatTestSchemaTables() throws ArcException {

		String tableIn = "public.table_test_in";
		String tableOut = "public.table_test_out";
		
		// create a table with 5 records and 2 columns (i,j)
		u.executeImmediate(c, "CREATE TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i");
		
		// create tableOut as an empty image of tableIn
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut));
		testMetadataAndNumberOfRecords(tableOut, 0, this.expectedColumnsForTest);
		u.dropTable(c, tableOut);
		
		// create tableOut as an empty image of tableIn (false argument)
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut, false));
		testMetadataAndNumberOfRecords(tableOut, 0, this.expectedColumnsForTest);
		u.dropTable(c, tableOut);

		// create tableOut as the exact image of tableIn (true argument)
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut, true));
		testMetadataAndNumberOfRecords(tableOut, this.expectedNumberOfRecordsForTest, this.expectedColumnsForTest);
		
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
		u.executeImmediate(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i");
		
		// create tableOut as an empty image of tableIn
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut));
		testMetadataAndNumberOfRecords(tableOut, 0, this.expectedColumnsForTest);
		u.dropTable(c, tableOut);
		
		// create tableOut as an empty image of tableIn (false argument)
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut, false));
		testMetadataAndNumberOfRecords(tableOut, 0, this.expectedColumnsForTest);
		u.dropTable(c, tableOut);

		// create tableOut as the exact image of tableIn (true argument)
		u.executeImmediate(c, ServiceTableOperation.creationTableResultat(tableIn, tableOut, true));
		testMetadataAndNumberOfRecords(tableOut, this.expectedNumberOfRecordsForTest, this.expectedColumnsForTest);
		
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
		u.executeImmediate(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i");

		// execute createTableInherit to create the table duplication
		u.executeImmediate(c, ServiceTableOperation.createTableInherit(tableIn, tableOut));
		
		// test
		// tableOut must exists
		testTableExists(tableOut,1);
		// the data must be same as tableIn
		testMetadataAndNumberOfRecords(tableOut,this.expectedNumberOfRecordsForTest, this.expectedColumnsForTest);
		u.dropTable(c, tableIn, tableOut);
	
		
		// test 2: tableIn is empty
		u.executeImmediate(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i where false");
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
		u.executeImmediate(c, "CREATE TABLE "+tableOfIdSource+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i");

		// creation de la table temporaire de données relative copie de la table de données du fichier
		u.executeImmediate(c, ServiceTableOperation.createTableTravailIdSource(tableIn, tableOut, idSource));
				
		// test if content is the same
		testMetadataAndNumberOfRecords(tableOut, this.expectedNumberOfRecordsForTest, this.expectedColumnsForTest);
		u.dropTable(c, tableOfIdSource, tableOut);

		
		// testing with extra columns definition
		// creation de la table de données relative au fichier
		u.executeImmediate(c, "CREATE TABLE "+tableOfIdSource+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i");

		// creation de la table temporaire de données relative copie de la table de données du fichier
		u.executeImmediate(c, ServiceTableOperation.createTableTravailIdSource(tableIn, tableOut, idSource, "null::text as k, 8::int as l"));
		
		String[] expectedColumns = new String[] {"i", "j", "k", "l"};
		testMetadataAndNumberOfRecords(tableOut, this.expectedNumberOfRecordsForTest, expectedColumns);
		
		u.dropTable(c, tableOfIdSource, tableOut);

	}
	
	

}
