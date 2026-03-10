package fr.insee.arc.utils.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class InputSecurityTest {

	@Test
	public void testServiceHashFileNameIsUtilityClass() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(InputSecurity.class);
	}
	
	@Test
	public void testFormatAsDatabaseIdentifier() {
		
		String input = null;
		assertNull(InputSecurity.formatAsDatabaseIdentifier(input));
		
		assertEquals("var_table_metier",InputSecurity.formatAsDatabaseIdentifier("var_table_metier"));
		assertEquals("vartable_metier",InputSecurity.formatAsDatabaseIdentifier("var-table_metier"));

		assertEquals("vartable_metier",InputSecurity.formatAsDatabaseIdentifier("\n -var-table_metier"));

		// remove space and ; and other chars
		assertEquals("var_tablemetier",InputSecurity.formatAsDatabaseIdentifier("var_table metier"));
		assertEquals("dropdatabasetoto",InputSecurity.formatAsDatabaseIdentifier("drop database toto;"));
		assertEquals("var_table_metier$10",InputSecurity.formatAsDatabaseIdentifier("var_table_metier$10"));
		assertEquals("var_tableMETIER",InputSecurity.formatAsDatabaseIdentifier("var_table METIER"));

		assertEquals("var_tablemetier",InputSecurity.formatAsDatabaseIdentifier("000var_table metier"));

		List<String> guiInputsNull=null;
		assertNull(InputSecurity.formatAsDatabaseIdentifier(guiInputsNull));
		
		List<String> guiInputs = Arrays.asList("__var_table_metier$$", "drop database toto;");
		List<String> guiInputsReformat = InputSecurity.formatAsDatabaseIdentifier(guiInputs);
		assertEquals("var_table_metier$$", guiInputsReformat.get(0));
		assertEquals("dropdatabasetoto", guiInputsReformat.get(1));
				
	}

}
