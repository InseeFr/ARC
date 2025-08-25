package fr.insee.arc.utils.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class GenericPreparedStatementBuilderTest extends InitializeQueryTest {

	@Test
	public void copyFromGenericBean() throws ArcException {

		// create the test table
		assertEquals("test.table_test", createSimpleTableTest(c));
		
		// the content will grab the first 10 lines of test table
		GenericBean gb=	new GenericBean(UtilitaireDao.get(0).executeRequest(c, new GenericPreparedStatementBuilder("SELECT * FROM test.table_test where id<=10")));
		
		// copy the content into a target table using copyFromGenericBean
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.copyFromGenericBean("test.table_test_copy", gb);
		UtilitaireDao.get(0).executeRequest(c, query);
		
		// test result
		testMetadataAndNumberOfRecords("test.table_test_copy", 10, new String[] {"id", "val", "arr", "dd"});

		// drop test table
		dropSimpleTableTest(c);
		
	}

}
