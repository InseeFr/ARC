package fr.insee.arc.utils.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class FormatTest {

	@Test
	public void testServiceHashFileNameIsUtilityClass() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(Format.class);
	}


	@Test
	public void untokenizeTestWithCollection()
	{
		// test empty collection
		assertEquals("",Format.untokenize(new ArrayList<String>(), ","));
		
		// test untokenize standart use case 
		assertEquals("a,b,c",Format.untokenize(new ArrayList<String>(Arrays.asList("a","b","c")), ","));
	}

	@Test
	public void tokenizeAndTrim()
	{
		assertTrue(Arrays.equals(new String[] {"col1","col2","col3","col4","col5"},Format.tokenizeAndTrim(" col1, col2 , 	col3,col4,	col5",",")));

	}
	
	
	@Test
	public void toBdVal()
	{
		assertTrue(Arrays.equals(new String[] {"v_col1","v_col2","v_col3","v_col4","v_col5"},Format.toBdVal(Format.tokenizeAndTrim(" col1, col2 , 	col3,col4,	col5",","))));

	}
	
	
	@Test
	public void untokenize()
	{
		assertEquals("v_col1,v_col2,v_col3,v_col4,v_col5", Format.untokenize(Arrays.asList("v_col1","v_col2","v_col3","v_col4","v_col5"), ","));
		assertEquals("", Format.untokenize(null, ","));

	}

}
