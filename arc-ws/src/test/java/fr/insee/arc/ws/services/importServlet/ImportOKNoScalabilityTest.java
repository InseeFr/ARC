package fr.insee.arc.ws.services.importServlet;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.ws.services.importServlet.dao.InitializeTestDataNoScalability;

public class ImportOKNoScalabilityTest {

	@BeforeAll
	public static void setup() throws SQLException, ArcException {
		InitializeTestDataNoScalability.destroyTestData();
		InitializeTestDataNoScalability.initializeTestData(true);
	}

	@AfterAll
	public static void tearDown() throws SQLException, ArcException {
		InitializeTestDataNoScalability.destroyTestData();
	}

	@Test
	public void testExecuteFamilyNotValid() throws ArcException, UnsupportedEncodingException {
		assertThrows(ArcException.class, () -> {
			RunImport.testExecuteFamilyNotValid();
		});
	}

	@Test
	public void testExecute() throws ArcException, IOException {
		RunImport.testExecute();
	}

}
