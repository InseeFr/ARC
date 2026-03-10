package fr.insee.arc.ws.services.importServlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.ws.services.importServlet.dao.InitializeTestDataNoScalability;

public class ImportOKNoScalabilityTest {

	@BeforeClass
	public static void setup() throws SQLException, ArcException {
		InitializeTestDataNoScalability.destroyTestData();
		InitializeTestDataNoScalability.initializeTestData(true);
	}

	@AfterClass
	public static void tearDown() throws SQLException, ArcException {
		InitializeTestDataNoScalability.destroyTestData();
	}

	@Test(expected = ArcException.class)
	public void testExecuteFamilyNotValid() throws ArcException, UnsupportedEncodingException {
		RunImport.testExecuteFamilyNotValid();
	}

	@Test
	public void testExecute() throws ArcException, IOException {
		RunImport.testExecute();
	}

}
