package fr.insee.arc.ws.services.importServlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.ws.services.importServlet.bo.ExecuteStep;
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
