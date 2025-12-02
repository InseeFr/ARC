package fr.insee.arc.ws.services.importServlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.ws.services.importServlet.bo.ExecuteStep;

public class RunImport {

	public static void testExecuteFamilyNotValid() throws UnsupportedEncodingException, ArcException {
		JSONObject clientJsonInput = new JSONObject(
				"{\"familleNorme\":\"RESIL\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");
		ExecuteStep.executeImportStep1(clientJsonInput);
	}
	
	
	public static void testExecute() throws ArcException, IOException {

		// parameters sent by client for step 1
		JSONObject clientJsonInputStep1 = new JSONObject(
				"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");

		// response token will be used by client to invoke step2
		String arcResponseStep1 = ExecuteStep.executeImportStep1(clientJsonInputStep1);

		testCreateAndDropWsPending();
		testCreateTableNmcl();
		testCreateTableVarMetier();
		testCreateTableTableMetier();
		testCreateTableTableFamille();
		testCreateTableTablePeriodicite();

		// test ws Iteration
		testWsIteration(arcResponseStep1);

		// test that client had been marked in pilotage
		testClientMarkedInPilotage();

	}

	private static void testClientMarkedInPilotage() throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append(
				"SELECT distinct client[1] as client from arc_bas1.pilotage_fichier where id_source in ('file1_to_retrieve.xml','file2_to_retrieve.xml');");

		String clientMarkedInPilotage = UtilitaireDao.get(0).getString(null, query);

		assertEquals("ARTEMIS", clientMarkedInPilotage);
	}

	/**
	 * iterate through webservice call until the end test the first table to be
	 * retrieved
	 * 
	 * @param arcResponseStep1
	 * @throws IOException
	 * @throws ArcException
	 */
	private static void testWsIteration(String arcResponseStep1) throws IOException, ArcException {
		boolean sortie = false;
		boolean first = true;

		do {
			String arcResponseStep2 = invokeStep2(arcResponseStep1);
			// arcResponse2 return table,ame and DDL of the table
			// tablename is the first token from the arcResponse2
			String tableBucket = ManipString.substringBeforeFirst(arcResponseStep2, " ");

			sortie = tableBucket.equals("");

			if (sortie) {
				break;
			}

			ByteArrayOutputStream arcResponseStep3 = invokeStep3(tableBucket);
			String outputStep3 = "";

			try (InputStream is = new ByteArrayInputStream(arcResponseStep3.toByteArray());
					GZIPInputStream zis = new GZIPInputStream(is);) {
				outputStep3 = IOUtils.toString(zis, StandardCharsets.UTF_8);
			}

			if (first) {
				// ws info must be the first table to be retrieved
				assertEquals(arcResponseStep1 + "_ws_info  client text, timestamp text", arcResponseStep2);
				// first info in the csv table of ws_info is client name
				assertEquals("ARTEMIS", ManipString.substringBeforeFirst(outputStep3, ";"));
			}

			first = false;

		} while (!sortie);

	}

	private static ByteArrayOutputStream invokeStep3(String tableResponseStep2)
			throws UnsupportedEncodingException, ArcException {
		JSONObject clientJsonInputStep3 = new JSONObject(
				"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"tableName\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\""
						+ tableResponseStep2 + "\",\"environnement\":\"arc_bas1\",\"type\":\"jsonwsp/request\"}");
		return ExecuteStep.executeImportStep3(clientJsonInputStep3);
	}

	private static String invokeStep2(String arcResponseStep1) throws UnsupportedEncodingException, ArcException {
		// parameters sent by client for step 2
		// it use response token provided as response of step1
		JSONObject clientJsonInputStep2 = new JSONObject(
				"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"tableName\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\""
						+ arcResponseStep1 + "\",\"environnement\":\"arc_bas1\",\"type\":\"jsonwsp/request\"}");

		return ExecuteStep.executeImportStep2(clientJsonInputStep2);
	}

	private static void testCreateAndDropWsPending() throws ArcException {

		// check that the parallel thread that create tables drop the table ws_pending

		// it should be done in less than 50 iteration, test data is very little
		int maxIteration = 50;
		int i = 0;

		while (i < maxIteration
				&& UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_ws_pending")) {
			i++;
			UtilitaireDao.get(0).executeRequest(InitializeQueryTest.c, "SELECT pg_sleep(1);");
		}

		assertTrue(i > 0);
		assertTrue(i < maxIteration);
	}

	private static void testCreateTableNmcl() throws ArcException {
		// table image created should be like
		// arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_nmcl_table1"));
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_nmcl_table2"));
	}

	private static void testCreateTableVarMetier() throws ArcException {
		// table image created should be like
		// arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_mod_variable_metier"));
	}

	private static void testCreateTableTableMetier() throws ArcException {
		// table image created should be like
		// arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_mod_table_metier"));
	}

	private static void testCreateTableTableFamille() throws ArcException {
		// table image created should be like
		// arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_ext_mod_famille"));
	}

	private static void testCreateTableTablePeriodicite() throws ArcException {
		// table image created should be like
		// arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_ext_mod_periodicite"));
	}


	
}
