package fr.insee.arc.utils.webutils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WebSecurityTest {


	@Test
	public void isOverloaded1()
	{
		assertEquals(false, WebSecurity.isOverloaded("${i am a properties value that haven't been overloaded by environnement}"));
	}

	@Test
	public void isOverloaded0()
	{
		assertEquals(true, WebSecurity.isOverloaded("i am a properties value that have been overloaded by environement or by other ways"));
	}


	
	
}
