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
	
	/**
	 * check the table meta data and that the table is empty
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

}
