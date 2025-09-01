package fr.insee.arc.utils.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class FormatSQLRenamingMethodsTest {
	
	@Test
	public void temporaryTableNameTest()
	{
		assertTrue(FormatSQL.temporaryTableName("schema.matable$tmp$5380359056$7281", "002").startsWith("schema.matable_002$tmp$"));
	}
	
	@Test
	public void imageObjectNameImgTest()
	{
		assertEquals("schema.myobject_img", FormatSQL.imageObjectName("schema.myobject"));
		assertEquals("myobject_img", FormatSQL.imageObjectName("myobject"));

		String tableName63Bytes= "123456789012345678901234567890123456789012345678901234567890123";
		assertEquals(63,tableName63Bytes.length());
		assertEquals("56789012345678901234567890123456789012345678901234567890123_img",
				FormatSQL.imageObjectName(tableName63Bytes));

		String tableName63BytesWithSchema= "arc_bas2."+tableName63Bytes;
		assertEquals("arc_bas2.56789012345678901234567890123456789012345678901234567890123_img",
				FormatSQL.imageObjectName(tableName63BytesWithSchema));

		String tableName61Bytes= "1234567890123456789012345678901234567890123456789012345678901";
		assertEquals(61,tableName61Bytes.length());
		assertEquals("34567890123456789012345678901234567890123456789012345678901_img",
				FormatSQL.imageObjectName(tableName61Bytes));

		System.out.println(FormatSQL.imageObjectName(tableName61Bytes, "000"));
		assertEquals("7890123456789012345678901234567890123456789012345678901_img000",
				FormatSQL.imageObjectName(tableName61Bytes, "000"));
		assertEquals(63,FormatSQL.imageObjectName(tableName61Bytes, "000").length());
		
	}

}
