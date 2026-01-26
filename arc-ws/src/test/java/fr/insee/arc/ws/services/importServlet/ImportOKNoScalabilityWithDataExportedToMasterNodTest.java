package fr.insee.arc.ws.services.importServlet;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.ws.services.importServlet.dao.InitializeTestDataNoScalability;


/**
 * In that case files must be retrieved if they had been marked with EXPORT
 */
public class ImportOKNoScalabilityWithDataExportedToMasterNodTest {

	@BeforeAll
	public static void setup() throws SQLException, ArcException {
		InitializeTestDataNoScalability.destroyTestData();
		InitializeTestDataNoScalability.initializeTestData(true);
		InitializeTestDataNoScalability.mappingTablesDataIsSentToMasterNod();
	}

	@AfterAll
	public static void tearDown() throws SQLException, ArcException {
		InitializeTestDataNoScalability.destroyTestData();
	}

	@Test
	public void testExecuteFamilyNotValid() {
		assertThrows(ArcException.class, RunImport::testExecuteFamilyNotValid);
	}

	@Test
	public void testExecute() throws ArcException, IOException {
		RunImport.testExecute(
				"file2_to_retrieve.xml:{DSNFLASH},file3_to_retrieve.xml:{EXPORT,ARTEMIS},file_not_to_retrieve_when_reprise_false.xml:{ARTEMIS}"
				);
	}

}
