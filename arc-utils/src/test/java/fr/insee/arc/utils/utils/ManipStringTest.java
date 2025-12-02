package fr.insee.arc.utils.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class ManipStringTest extends ManipString {

	
	// isStringNul
	@Test
	public void isStringNull1() {
		// yes null
		assertEquals(true, isStringNull(null));
	}

	@Test
	public void isStringNull2() {
		// yes empty
		assertEquals(true, isStringNull(""));
	}

	@Test
	public void isStringNull3() {
		// false not empty
		assertEquals(false, isStringNull(" "));
	}

	// substringBeforeFirst

	@Test
	public void substringBeforeFirst1() {
		// null input string -> return null
		assertEquals(null, substringBeforeFirst(null, "-"));
	}

	@Test
	public void substringBeforeFirst2() {
	// null separator -> return all string
	assertEquals("abc", substringBeforeFirst("abc", null));
	}
	
	@Test
	public void substringBeforeFirst3() {
	// separator not found -> return all string
	assertEquals("abc", substringBeforeFirst("abc", "-"));
	}
	
	@Test
	public void substringBeforeFirst4() {
		// nominal use case
		assertEquals("az", substringBeforeFirst("az-b-c", "-"));
	}
	

	// substringBeforeLast
	@Test
	public void substringBeforeLast1() {
		// null input string -> return null
		assertEquals(null, ManipString.substringBeforeLast(null, "-"));
	}
	
	@Test
	public void substringBeforeLast2() {
		// null separator -> return all string
		assertEquals("abc", ManipString.substringBeforeLast("abc", null));
	}
	
	@Test
	public void substringBeforeLast3() {
		// separator not found -> return all string
		assertEquals("abc", ManipString.substringBeforeLast("abc", "-"));
	}
	
	@Test
	public void substringBeforeLast4() {
		// nominal use case
		assertEquals("a-b", ManipString.substringBeforeLast("a-b-c", "-"));
	}

	// substringAfterFirst
	@Test
	public void substringAfterFirst1() {
		// null input string -> return null
		assertEquals(null, ManipString.substringAfterFirst(null, "-"));
	}

	@Test
	public void substringAfterFirst2() {
		// null separator -> return all string
		assertEquals("abc", ManipString.substringAfterFirst("abc", null));
	}
	
	@Test
	public void substringAfterFirst3() {
		// separator not found -> return all string
		assertEquals("abc", ManipString.substringAfterFirst("abc", "-"));
	}
	
	@Test
	public void substringAfterFirst4() {
		// nominal use case
		assertEquals("b-c", ManipString.substringAfterFirst("a-b-c", "-"));
	}
	

	// substringAfterLast
	@Test
	public void substringAfterLast1() {
		// null input string -> return null
		assertEquals(null, ManipString.substringAfterLast(null, "-"));
	}

	@Test
	public void substringAfterLast2() {
		// null separator -> return all string
		assertEquals("abc", ManipString.substringAfterLast("abc", null));
	}
	
	@Test
	public void substringAfterLast3() {
		// separator not found -> return all string
		assertEquals("abc", ManipString.substringAfterLast("abc", "-"));
	}
	
	@Test
	public void substringAfterLast4() {
		// nominal use case
		assertEquals("c", ManipString.substringAfterLast("a-b-c", "-"));
	}
	
	// parseInteger
	@Test
	public void parseInteger1() {
		// not a number
		assertEquals((Integer)null,ManipString.parseInteger("1 am not a number"));
		// positive number with + sign
		assertEquals((Integer)10,ManipString.parseInteger("+10"));
		// decimal number returns null
		assertEquals(null,ManipString.parseInteger("10.8"));
	}
	
	@Test
	public void parseInteger2() {
		// positive number with + sign
		assertEquals((Integer)10,ManipString.parseInteger("+10"));
	}
	
	@Test
	public void parseInteger3() {
		// decimal number returns null
		assertEquals(null,ManipString.parseInteger("10.8"));
	}

	
	// redoEntryName
	@Test
	public void redoEntryName1() {
		// rename an archive entry to a temporary name
		assertEquals("a.tar.gz§depot§c.xml",ManipString.redoEntryName("a.tar.gz/depot\\c.xml"));
	}
	
	
	// stringToList
	@Test
    public void stringToList1() {
		assertEquals(Arrays.asList("a","b","cde"),ManipString.stringToList("a,b,cde",","));
    }
	
	@Test
	 public void stringToList2() {
		assertThrows(NullPointerException.class, () ->
		{
		ManipString.stringToList(null,",");
		});
    }
	
	@Test
    public void stringToList3() {
		assertEquals(Arrays.asList("a","b","cde"),ManipString.stringToList("a","b","cde"));
    }
	
}
