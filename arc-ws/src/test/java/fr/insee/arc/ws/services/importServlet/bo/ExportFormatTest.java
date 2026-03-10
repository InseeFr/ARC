package fr.insee.arc.ws.services.importServlet.bo;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExportFormatTest {

	@Test
	public void exportFormat_isParquet_test() {
		assertTrue(ExportFormat.PARQUET.isParquet());
		assertFalse(ExportFormat.BINARY.isParquet());

	}

}
