package fr.insee.arc.utils.webutils;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheckKeycloakTest {

	@Test
	public void isKeycloakActiveFalse()
	{
		CheckKeycloak w = new CheckKeycloak();
		assertEquals(false, w.isKeycloakActive());
	}
	
	@Test
	public void isKeycloakActiveTrue()
	{
		CheckKeycloak w = new CheckKeycloak();
		w.setKeycloak("realm");
		assertEquals(true, w.isKeycloakActive());
	}
}
