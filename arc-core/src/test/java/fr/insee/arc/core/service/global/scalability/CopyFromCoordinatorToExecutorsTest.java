package fr.insee.arc.core.service.global.scalability;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class CopyFromCoordinatorToExecutorsTest extends InitializeQueryTest {

	/**
	 * This test copy the test table table_test from connection c to connection e1
	 * @throws ArcException
	 * @throws SQLException
	 */
	@Test
	public void execCopyFromTableTest() throws ArcException, SQLException {
		
		buildPropertiesWithTwoExecutors(".");
		
		// create the test table in databases
		createSimpleTableTest(c);
		createSimpleTableTest(e1);
		createSimpleTableTest(e2);
		
		CopyFromCoordinatorToExecutors copy = new CopyFromCoordinatorToExecutors();
		copy.copyWithTee("test.table_test", "test.table_test_copy");
		
		// compare table_test with table_test_copy on each nod i.e. on each connection
		int numberOfRecordInInputTable = UtilitaireDao.get(0).getInt(c, "SELECT count(*) from test.table_test");
		int numberOfRecordInCopyNod1 = UtilitaireDao.get(0).getInt(e1, "SELECT count(*) from test.table_test_copy");
		int numberOfRecordInCopyNod2 = UtilitaireDao.get(0).getInt(e2, "SELECT count(*) from test.table_test_copy");

		assertEquals(numberOfRecordInInputTable, numberOfRecordInCopyNod1);
		assertEquals(numberOfRecordInInputTable, numberOfRecordInCopyNod2);
		
		// drop test objects
		dropSimpleTableTest(c);		
		dropSimpleTableTest(e1);		
		dropSimpleTableTest(e2);		

	}
	
	
}
