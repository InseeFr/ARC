package fr.insee.arc.ws.services.importServlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.ws.services.importServlet.dao.InitializeTestDataScalability;

public class ImportOKScalabilityTest {

	@BeforeClass
	public static void setup() throws SQLException, ArcException {
		InitializeTestDataScalability.destroyTestData();
		InitializeTestDataScalability.initializeTestData(true);
	}

	@AfterClass
	public static void tearDown() throws SQLException, ArcException {
		InitializeTestDataScalability.destroyTestData();
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
