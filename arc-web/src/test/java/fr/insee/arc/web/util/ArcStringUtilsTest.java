package fr.insee.arc.web.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ArcStringUtilsTest {

	//cleanUpVariable(String)
	@Test
	public void variableIsCleaned() {
		assertEquals("toto", ArcStringUtils.cleanUpVariable(" ToTo "));
	}

	@Test
	public void cleanVariableIsIdentical() {
		assertEquals("toto", ArcStringUtils.cleanUpVariable("toto"));
	}

	@Test
	public void nulllVariableIsIgnored() {
		assertEquals(null, ArcStringUtils.cleanUpVariable(null));
	}
	
}
