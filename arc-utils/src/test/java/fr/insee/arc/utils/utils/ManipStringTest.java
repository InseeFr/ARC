package fr.insee.arc.utils.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ManipStringTest {

	@Test
	public void isStringNullYesNull() {
		assertEquals(true, ManipString.isStringNull(null));
	}

	@Test
	public void isStringNullYesEmpty() {
		assertEquals(true, ManipString.isStringNull(""));
	}

	@Test
	public void isStringNullNo() {
		assertEquals(false, ManipString.isStringNull("hi"));
	}

	// substringBeforeFirst
	@Test
	public void substringBeforeFirstNullString() {
		assertEquals(null, ManipString.substringBeforeFirst(null, "-"));
	}


	@Test
	public void substringBeforeFirstNullSep() {
		assertEquals("abc", ManipString.substringBeforeFirst("abc", null));
	}

	@Test
	public void substringBeforeFirstNoSep() {
		assertEquals("abc", ManipString.substringBeforeFirst("abc", "-"));
	}

	@Test
	public void substringBeforeFirstOk() {
		assertEquals("a", ManipString.substringBeforeFirst("a-b-c", "-"));
	}

	// substringBeforeLast
	@Test
	public void substringBeforeLastNullString() {
		assertEquals(null, ManipString.substringBeforeLast(null, "-"));
	}


	@Test
	public void substringBeforeLastNullSep() {
		assertEquals("abc", ManipString.substringBeforeLast("abc", null));
	}

	@Test
	public void substringBeforeLastNoSep() {
		assertEquals("abc", ManipString.substringBeforeLast("abc", "-"));
	}

	@Test
	public void substringBeforeLastOk() {
		assertEquals("a-b", ManipString.substringBeforeLast("a-b-c", "-"));
	}

	// substringAfterFirst
	@Test
	public void substringAfterFirstNullString() {
		assertEquals(null, ManipString.substringAfterFirst(null, "-"));
	}


	@Test
	public void substringAfterFirstNullSep() {
		assertEquals("abc", ManipString.substringAfterFirst("abc", null));
	}

	@Test
	public void substringAfterFirstNoSep() {
		assertEquals("abc", ManipString.substringAfterFirst("abc", "-"));
	}

	@Test
	public void substringAfterFirstOk() {
		assertEquals("b-c", ManipString.substringAfterFirst("a-b-c", "-"));
	}

	// substringAfterLast
	@Test
	public void substringAfterLastNullString() {
		assertEquals(null, ManipString.substringAfterLast(null, "-"));
	}


	@Test
	public void substringAfterLastNullSep() {
		assertEquals("abc", ManipString.substringAfterLast("abc", null));
	}

	@Test
	public void substringAfterLastNoSep() {
		assertEquals("abc", ManipString.substringAfterLast("abc", "-"));
	}

	@Test
	public void substringAfterLastOk() {
		assertEquals("c", ManipString.substringAfterLast("a-b-c", "-"));
	}

	// compress
	@Test
	public void compress() {
		String testValue1="arêtes de po¹ssôn";
		assertEquals(testValue1,ManipString.decompress(ManipString.compress(testValue1)));
	}
	
}
