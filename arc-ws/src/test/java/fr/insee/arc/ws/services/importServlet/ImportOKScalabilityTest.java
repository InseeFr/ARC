package fr.insee.arc.ws.services.importServlet;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.ws.services.importServlet.dao.InitializeTestDataScalability;

public class ImportOKScalabilityTest {

	@BeforeAll
	public static void setup() throws SQLException, ArcException {
		InitializeTestDataScalability.destroyTestData();
		InitializeTestDataScalability.initializeTestData(true);
	}

	@AfterAll
	public static void tearDown() throws SQLException, ArcException {
		InitializeTestDataScalability.destroyTestData();
	}

	@Test
	public void testExecuteFamilyNotValid() {
		assertThrows(ArcException.class, RunImport::testExecuteFamilyNotValid);
	}

	@Test
	public void testExecute() throws ArcException, IOException {
		RunImport.testExecute(
				"file1_not_to_retrieve_when_reprise_false.xml:{ARTEMIS},file1_to_retrieve.xml:{ARTEMIS},file2_not_to_retrieve_when_reprise_false.xml:{ARTEMIS},file2_to_retrieve.xml:{DSNFLASH,ARTEMIS}"
				);
	}

}
