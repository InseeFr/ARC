package fr.insee.arc.utils.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;


public class ArcExceptionMessageTest {

	@Test
	public void testExceptionFormateerString() {
		assertEquals("Le fichier a.xml n'a pas pu être renommé vers b.xml ",ArcExceptionMessage.FILE_RENAME_FAILED.formatException("a.xml","b.xml"));
	}

	@Test
	public void testExceptionFormateerFile() {
		File a=new File("a.xml");
		File b=new File("b.xml");
		assertEquals("Le fichier a.xml n'a pas pu être renommé vers b.xml ",ArcExceptionMessage.FILE_RENAME_FAILED.formatException(a,b));
	}
	
}
