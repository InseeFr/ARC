package fr.insee.arc.utils.dao;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class GenericPreparedStatementBuilderSqlTest {

	@Test
	public void sqlListeOfColumns() {
		
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append(query.sqlListeOfColumns(Arrays.asList("col1","col2","col3")));
		assertEquals("col1,col2,col3", query.getQueryWithParameters());
		
	}
	
	@Test
	public void sqlListeOfValues() {
		
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append(query.sqlListeOfValues(Arrays.asList("val1","val2","val3")));
		assertEquals("'val1','val2','val3'", query.getQueryWithParameters());
	}
	

	@Test
	public void sqlListeOfValuesWithBraces() {
		
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append(query.sqlListeOfValues(Arrays.asList("val1","val2","val3"), "(", ")"));
		assertEquals("('val1'),('val2'),('val3')", query.getQueryWithParameters());
	}

}
