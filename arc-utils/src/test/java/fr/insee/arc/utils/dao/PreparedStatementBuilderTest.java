package fr.insee.arc.utils.dao;



import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;


public class PreparedStatementBuilderTest {

	@Test
	public void append() {
		
		// test query with empty parameters
		String query="SELECT * FROM my_table";
		PreparedStatementBuilder p=new PreparedStatementBuilder();
		
		// test the query content and the bind parameters are empty
		assertEquals(query, p.append(query).getQuery().toString());
		assertEquals((new ArrayList<String>()).toString(),p.getParameters().toString());

	}

	@Test
	public void quoteText() {
		
		PreparedStatementBuilder p=new PreparedStatementBuilder();
		String my_sql_value="MY VALUE";
		
		p.quoteText(my_sql_value);
		
		// test for null value
		assertEquals((new ArrayList<String>(Arrays.asList(my_sql_value))).toString(),p.getParameters().toString());

		
	}

	
	@Test
	public void sqlEqual() {
		
		PreparedStatementBuilder p=new PreparedStatementBuilder();
		
		// test for null value
		assertEquals(" is null ",p.sqlEqual(null,"text"));
		assertEquals((new ArrayList<String>()).toString(),p.getParameters().toString());
		
		// test for int value
		assertEquals(" =   ?   ::int ",p.sqlEqual("10","int"));
		assertEquals((new ArrayList<String>(Arrays.asList("10"))).toString(),p.getParameters().toString());
		
	}
	
	

	@Test
	public void hashcode() {
		System.out.println(Math.abs(Integer.MIN_VALUE+1));
		assertEquals(true,true);
	}
	
	
}
