package fr.insee.arc.ws.services.importServlet.bo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ExportFormatTest {

	@Test
	public void exportFormat_isParquet_test() {
		assertTrue(ExportFormat.PARQUET.isParquet());
		assertFalse(ExportFormat.BINARY.isParquet());

	}

}
