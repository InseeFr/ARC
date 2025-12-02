package fr.insee.arc.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import fr.insee.arc.web.gui.all.util.ArcStringUtils;

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
