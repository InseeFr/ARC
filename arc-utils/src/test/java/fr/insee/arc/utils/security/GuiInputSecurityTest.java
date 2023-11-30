package fr.insee.arc.utils.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class GuiInputSecurityTest {

	@Test
	public void testServiceHashFileNameIsUtilityClass() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(GuiInputSecurity.class);
	}
	
	@Test
	public void testFormatAsDatabaseIdentifier() {
		
		String input = null;
		assertNull(GuiInputSecurity.formatAsDatabaseIdentifier(input));
		
		assertEquals("var_table_metier",GuiInputSecurity.formatAsDatabaseIdentifier("var_table_metier"));
		
		// remove space and ; and other chars
		assertEquals("var_tablemetier",GuiInputSecurity.formatAsDatabaseIdentifier("var_table metier"));
		assertEquals("dropdatabasetoto",GuiInputSecurity.formatAsDatabaseIdentifier("drop database toto;"));
		assertEquals("var_table_metier$10",GuiInputSecurity.formatAsDatabaseIdentifier("var_table_metier$10"));
		assertEquals("var_tableMETIER",GuiInputSecurity.formatAsDatabaseIdentifier("var_table METIER"));

		// remove trailing $ and _
		assertEquals("var_table_metier",GuiInputSecurity.formatAsDatabaseIdentifier("__var_table_metier$$"));
		assertEquals("var_table_metier",GuiInputSecurity.formatAsDatabaseIdentifier("$var_table_metier_$"));
		assertEquals("var_table_metier",GuiInputSecurity.formatAsDatabaseIdentifier("$_$var_table_metier$__;"));

		List<String> guiInputsNull=null;
		assertNull(GuiInputSecurity.formatAsDatabaseIdentifier(guiInputsNull));
		
		List<String> guiInputs = Arrays.asList("__var_table_metier$$", "drop database toto;");
		List<String> guiInputsReformat = GuiInputSecurity.formatAsDatabaseIdentifier(guiInputs);
		assertEquals("var_table_metier", guiInputsReformat.get(0));
		assertEquals("dropdatabasetoto", guiInputsReformat.get(1));
				
	}

}