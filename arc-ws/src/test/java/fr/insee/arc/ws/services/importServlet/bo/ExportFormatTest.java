package fr.insee.arc.ws.services.importServlet.bo;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExportFormatTest {

	@Test
	public void isCsv() {
		assertFalse(ExportFormat.isCsv("any"));
		assertFalse(ExportFormat.isCsv("binary"));
		assertTrue(ExportFormat.isCsv("csv_gzip"));
	}

}
