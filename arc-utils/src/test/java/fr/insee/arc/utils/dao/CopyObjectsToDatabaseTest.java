package fr.insee.arc.utils.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

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
		
		// create the test table that will be copied
		String testTable = createSimpleTableTest(c).get(0);
		
		// create the container of the copy
		String copyContainerTable = "tmp";
		
		
		// retrive data into a genericbean object
		// and copy the content of the genericbean into the table container
		GenericBean gb = new GenericBean(UtilitaireDao.get(0).executeRequest(c, "SELECT * FROM "+testTable));
		CopyObjectsToDatabase.execCopyFromGenericBean(e1, copyContainerTable, gb);

		int numberOfRecordInInputTable = countNumberOfRows(c, testTable);
		int numberOfRecordInCopy = countNumberOfRows(e1, copyContainerTable);

		// check that there is at least a record not to be a useless test
		assertTrue(numberOfRecordInInputTable>0);
		// the number of record of target table must be equals to the number of record in input table
		assertEquals(numberOfRecordInInputTable, numberOfRecordInCopy);
				
		// new copy without dropping the target copy container table
		CopyObjectsToDatabase.execCopyFromGenericBeanWithoutDroppingTargetTable(e1, copyContainerTable, gb);
		numberOfRecordInCopy = countNumberOfRows(e1, copyContainerTable);
		
		// the number of record of target table must be equals to 
		// the double of the number of records in input table
		assertEquals(numberOfRecordInInputTable*2, numberOfRecordInCopy);
		
		UtilitaireDao.get(0).executeRequest(e1, "DISCARD TEMP");
		
		// drop schema
		dropSimpleTableTest(c);		
	}

}
