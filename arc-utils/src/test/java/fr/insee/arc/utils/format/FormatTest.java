package fr.insee.arc.utils.format;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class FormatTest {

	@Test
	public void testServiceHashFileNameIsUtilityClass() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(Format.class);
	}

	@Test
	public void patchTest()
	{
		ArrayList<ArrayList<String>> content=new ArrayList<>();
		content.add(new ArrayList<String>(Arrays.asList("a","b","c")));
		content.add(new ArrayList<String>(Arrays.asList("d","e","f")));
		
		List<List<String>> patchedContent=Format.patch(content);
		
		// test the content of converted object is the same as the input object
		assertEquals(content.toString(), patchedContent.toString());
		
	}

	@Test
	public void untokenizeTestWithCollection()
	{
		// test empty collection
		assertEquals("",Format.untokenize(new ArrayList<String>(), ","));
		
		// test untokenize standart use case 
		assertEquals("a,b,c",Format.untokenize(new ArrayList<String>(Arrays.asList("a","b","c")), ","));
	}

	
	
}
