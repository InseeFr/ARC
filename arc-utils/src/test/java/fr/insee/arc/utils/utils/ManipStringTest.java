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
	
}
