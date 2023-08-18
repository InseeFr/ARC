package fr.insee.arc.utils.dao;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class GenericPreparedStatementBuilderTest extends InitializeQueryTest {

	@Test
	public void copyFromGenericBean() throws ArcException {
		UtilitaireDao.get(0).executeImmediate(c, "DROP SCHEMA IF EXISTS test CASCADE;");
		UtilitaireDao.get(0).executeImmediate(c, "CREATE SCHEMA test;");
		
		// create a test table with 26 lines
		UtilitaireDao.get(0).executeImmediate(c, "CREATE TABLE test.table_test as select i as id, chr(i+64) as val, array[i,i+1] as arr, current_date as dd from generate_series(1,26) i;");
		
		// the content will grab the first 10 lines of test table
		GenericBean gb=	new GenericBean(UtilitaireDao.get(0).executeRequest(c, new GenericPreparedStatementBuilder("SELECT * FROM test.table_test where id<=10")));
		
		// copy the content into a target table using copyFromGenericBean
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.copyFromGenericBean("test.table_test_copy", gb , false);
		UtilitaireDao.get(0).executeRequest(c, query);
		
		// test result
		testMetadataAndNumberOfRecords("test.table_test_copy", 10, new String[] {"id", "val", "arr", "dd"});
		UtilitaireDao.get(0).executeImmediate(c, "DROP SCHEMA IF EXISTS test CASCADE;");
		
	}

}
