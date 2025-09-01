package fr.insee.arc.utils.dao;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class CopyObjectsToDatabaseTest extends InitializeQueryTest {

	
	/**
	 * This test copy the test table table_test from connection c to connection e1
	 * @throws ArcException
	 * @throws SQLException
	 */
	@Test
	public void execCopyFromGenericBeanTest() throws ArcException, SQLException {
		
		buildPropertiesWithOneExecutor(".");
		
		// create the test table
		assertEquals("test.table_test", createSimpleTableTest(c));
		
		GenericBean gb = new GenericBean(UtilitaireDao.get(0).executeRequest(c, "SELECT * FROM test.table_test"));
		CopyObjectsToDatabase.execCopyFromGenericBean(e1, "tmp", gb);

		int numberOfRecordInInputTable = UtilitaireDao.get(0).getInt(c, "SELECT count(*) from test.table_test");
		int numberOfRecordInCopy = UtilitaireDao.get(0).getInt(e1, "SELECT count(*) from tmp");

		// check that there is at least a record not to be a useless test
		assertTrue(numberOfRecordInInputTable>0);
		// the number of record of target table must be equals to the number of record in input table
		assertEquals(numberOfRecordInInputTable, numberOfRecordInCopy);
				
		// new copy without dropping the target table
		CopyObjectsToDatabase.execCopyFromGenericBeanWithoutDroppingTargetTable(e1, "tmp", gb);
		numberOfRecordInCopy = UtilitaireDao.get(0).getInt(e1, "SELECT count(*) from tmp");
		
		// the number of record of target table must be equals to 
		// the double of the number of records in input table
		assertEquals(numberOfRecordInInputTable*2, numberOfRecordInCopy);
		
		UtilitaireDao.get(0).executeRequest(e1, "DISCARD TEMP");
		
		// drop schema
		dropSimpleTableTest(c);		
	}

	/**
	 * This test copy the test table table_test from connection c to connection e1
	 * @throws ArcException
	 * @throws SQLException
	 */
	@Test
	public void execCopyFromTableTest() throws ArcException, SQLException {
		
		buildPropertiesWithOneExecutor(".");
		
		// create the test table
		assertEquals("test.table_test", createSimpleTableTest(c));
		
		CopyObjectsToDatabase.execCopyFromTable(c, e1, "test.table_test", "tmp");

		int numberOfRecordInInputTable = UtilitaireDao.get(0).getInt(c, "SELECT count(*) from test.table_test");
		int numberOfRecordInCopy = UtilitaireDao.get(0).getInt(e1, "SELECT count(*) from tmp");

		// check that there is at least a record not to be a useless test
		assertTrue(numberOfRecordInInputTable>0);
		// the number of record of target table must be equals to the number of record in input table
		assertEquals(numberOfRecordInInputTable, numberOfRecordInCopy);
		
		// check that dblink extension had been deleted
		int numberOfDblinkExtension = UtilitaireDao.get(0).getInt(e1, "select count(*) from pg_extension where extname='dblink'");
		assertEquals(0, numberOfDblinkExtension);
				
		// new copy without dropping the target table
		CopyObjectsToDatabase.createExtensionDblink(e1);
		
		try {
			CopyObjectsToDatabase.execCopyFromTableWithoutDroppingTargetTableNorDblinkExtension(c, e1, "test.table_test", "tmp");
			// check that dblink extension still exists
			numberOfDblinkExtension = UtilitaireDao.get(0).getInt(e1, "select count(*) from pg_extension where extname='dblink'");
			assertEquals(1, numberOfDblinkExtension);
		}
		finally {
			CopyObjectsToDatabase.dropExtensionDblink(e1);
		}
		
		numberOfRecordInCopy = UtilitaireDao.get(0).getInt(e1, "SELECT count(*) from tmp");

		// the number of record of target table must be equals to 
		// the double of the number of records in input table
		assertEquals(numberOfRecordInInputTable * 2, numberOfRecordInCopy);
		
		UtilitaireDao.get(0).executeRequest(e1, "DISCARD TEMP");
		
		// drop schema
		dropSimpleTableTest(c);		
	}
	
}
