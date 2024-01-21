package fr.insee.arc.utils.webutils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WebSecurityTest {
	
	@Test
	public void isKeycloakActiveFalse()
	{
		WebSecurity w = new WebSecurity();
		assertEquals(false, w.isKeycloakActive());
	}
	
	@Test
	public void isKeycloakActiveTrue()
	{
		WebSecurity w = new WebSecurity();
		w.setKeycloak("realm", "url", "arc", "credentials");
		assertEquals(true, w.isKeycloakActive());
	}
	
}
