package fr.insee.arc.utils.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class FormatSQLTestRenamingMethods {
	
	@Test
	public void temporaryTableNameTest()
	{
		assertTrue(FormatSQL.temporaryTableName("schema.matable$tmp$5380359056$7281", "002").startsWith("schema.matable_002$tmp$"));
	}
	
	@Test
	public void imageObjectNameImg()
	{
		assertEquals("schema.myobject_img", FormatSQL.imageObjectName("schema.myobject"));
	}

}
