package fr.insee.arc.ws.services.importServlet.bo;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExportFormatTest {

	@Test
	public void isCsv() {
		assertFalse(ExportFormat.isCsv(ExportFormat.BINARY.toString()));
		assertTrue(ExportFormat.isCsv(ExportFormat.CSV_GZIP.toString()));
	}

}
