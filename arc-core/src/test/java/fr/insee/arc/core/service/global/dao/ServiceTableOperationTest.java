package fr.insee.arc.core.service.global.dao;

import org.junit.Test;

import fr.insee.arc.utils.dao.UtilitaireDao;
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
		UtilitaireDao.get(0).executeRequest(c, "CREATE TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i");
		
		// create tableOut as an empty image of tableIn
		UtilitaireDao.get(0).executeRequest(c, TableOperations.creationTableResultat(tableIn, tableOut));
		testMetadataAndNumberOfRecords(tableOut, 0, this.expectedColumnsForTest);
		UtilitaireDao.get(0).dropTable(c, tableOut);
		
		// create tableOut as an empty image of tableIn (false argument)
		UtilitaireDao.get(0).executeRequest(c, TableOperations.creationTableResultat(tableIn, tableOut, false));
		testMetadataAndNumberOfRecords(tableOut, 0, this.expectedColumnsForTest);
		UtilitaireDao.get(0).dropTable(c, tableOut);

		// create tableOut as the exact image of tableIn (true argument)
		UtilitaireDao.get(0).executeRequest(c, TableOperations.creationTableResultat(tableIn, tableOut, true));
		testMetadataAndNumberOfRecords(tableOut, this.expectedNumberOfRecordsForTest, this.expectedColumnsForTest);
		
		UtilitaireDao.get(0).dropTable(c, tableIn, tableOut);
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
		UtilitaireDao.get(0).executeRequest(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i");
		
		// create tableOut as an empty image of tableIn
		UtilitaireDao.get(0).executeRequest(c, TableOperations.creationTableResultat(tableIn, tableOut));
		testMetadataAndNumberOfRecords(tableOut, 0, this.expectedColumnsForTest);
		UtilitaireDao.get(0).dropTable(c, tableOut);
		
		// create tableOut as an empty image of tableIn (false argument)
		UtilitaireDao.get(0).executeRequest(c, TableOperations.creationTableResultat(tableIn, tableOut, false));
		testMetadataAndNumberOfRecords(tableOut, 0, this.expectedColumnsForTest);
		UtilitaireDao.get(0).dropTable(c, tableOut);

		// create tableOut as the exact image of tableIn (true argument)
		UtilitaireDao.get(0).executeRequest(c, TableOperations.creationTableResultat(tableIn, tableOut, true));
		testMetadataAndNumberOfRecords(tableOut, this.expectedNumberOfRecordsForTest, this.expectedColumnsForTest);
		
		UtilitaireDao.get(0).dropTable(c, tableIn, tableOut);
		
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
		UtilitaireDao.get(0).executeRequest(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i");

		// execute createTableInherit to create the table duplication
		UtilitaireDao.get(0).executeRequest(c, TableOperations.createTableInherit(tableIn, tableOut));
		
		// test
		// tableOut must exists
		testTableExists(tableOut,1);
		// the data must be same as tableIn
		testMetadataAndNumberOfRecords(tableOut,this.expectedNumberOfRecordsForTest, this.expectedColumnsForTest);
		UtilitaireDao.get(0).dropTable(c, tableIn, tableOut);
	
		
		// test 2: tableIn is empty
		UtilitaireDao.get(0).executeRequest(c, "CREATE TEMPORARY TABLE "+tableIn+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i where false");
		// execute createTableInherit to create the table duplication
		UtilitaireDao.get(0).executeRequest(c, TableOperations.createTableInherit(tableIn, tableOut));

		// test
		// tableOut shouldn't have been created and doesn't exist
		testTableExists(tableOut,0);
		UtilitaireDao.get(0).dropTable(c, tableIn, tableOut);
		
	}

	@Test
	public void createTableTravailIdSourceTest() throws ArcException
	{
		String idSource="mon_fichier.txt";
		String tableIn="public.chargement_ok";
		String tableOutTemporaire="table_temporaire_data";
		String tableOutPublic="public.table_temporaire_data";

		String tableOfIdSource=HashFileNameConversion.tableOfIdSource(tableIn, idSource);

		
		// creation de la table de données relative au fichier
		UtilitaireDao.get(0).executeRequest(c, "CREATE TABLE "+tableOfIdSource+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i");

		// creation de la table temporaire de données relative copie de la table de données du fichier
		UtilitaireDao.get(0).executeRequest(c, TableOperations.createTableTravailIdSource(tableIn, tableOutTemporaire, idSource));
				
		// test if content is the same
		testMetadataAndNumberOfRecords(tableOutTemporaire, this.expectedNumberOfRecordsForTest, this.expectedColumnsForTest);
		UtilitaireDao.get(0).dropTable(c, tableOfIdSource, tableOutTemporaire);

		
		// testing with extra columns definition
		// creation de la table de données relative au fichier
		UtilitaireDao.get(0).executeRequest(c, "CREATE TABLE "+tableOfIdSource+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i");

		// creation de la table temporaire de données relative copie de la table de données du fichier
		UtilitaireDao.get(0).executeRequest(c, TableOperations.createTableTravailIdSource(tableIn, tableOutTemporaire, idSource, "null::text as k, 8::int as l"));
		
		String[] expectedColumns = new String[] {"i", "j", "k", "l"};
		testMetadataAndNumberOfRecords(tableOutTemporaire, this.expectedNumberOfRecordsForTest, expectedColumns);
		
		UtilitaireDao.get(0).dropTable(c, tableOfIdSource, tableOutTemporaire);

		// creation de la table de données relative au fichier
		UtilitaireDao.get(0).executeRequest(c, "CREATE TABLE "+tableOfIdSource+" as SELECT i, i+1 as j FROM generate_series(1,"+this.expectedNumberOfRecordsForTest+") i");

		// creation de la table temporaire de données relative copie de la table de données du fichier
		UtilitaireDao.get(0).executeRequest(c, TableOperations.createTableTravailIdSource(tableIn, tableOutPublic, idSource));
				
		// test if content is the same
		testMetadataAndNumberOfRecords(tableOutTemporaire, this.expectedNumberOfRecordsForTest, this.expectedColumnsForTest);
		UtilitaireDao.get(0).dropTable(c, tableOfIdSource, tableOutTemporaire);
		
		
	}
	
	

}
