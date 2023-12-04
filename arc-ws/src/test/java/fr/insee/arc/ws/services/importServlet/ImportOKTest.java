package fr.insee.arc.ws.services.importServlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.dao.InitializeTestDataNoScalability;

public class ImportOKTest extends ServletArc {

	private static final long serialVersionUID = -7832574224892526397L;

	@BeforeClass
    public static void setup() throws SQLException, ArcException {
		InitializeTestDataNoScalability.destroyTestData();
		InitializeTestDataNoScalability.initializeTestData(true);
	}
	
	@AfterClass
    public static void tearDown() throws SQLException, ArcException {
		InitializeTestDataNoScalability.destroyTestData();
    }

	private String executeImportStep1(JSONObject clientJsonInput) throws ArcException
	{
		JSONObject clientJsonInputValidated= validateRequest(clientJsonInput);
		ImportStep1InitializeClientTablesService imp = new ImportStep1InitializeClientTablesService(clientJsonInputValidated);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		SendResponse sentResponse = new SendResponse(bos);
		imp.execute(sentResponse);
		return sentResponse.getWr().toString();
	}
	
	private String executeImportStep2(JSONObject clientJsonInput) throws ArcException
	{
		JSONObject clientJsonInputValidated= validateRequest(clientJsonInput);
		ImportStep2GetTableNameService imp = new ImportStep2GetTableNameService(clientJsonInputValidated);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		SendResponse sentResponse = new SendResponse(bos);
		imp.execute(sentResponse);
		return sentResponse.getWr().toString();
	}
	
	
	@Test(expected = ArcException.class)
	public void testExecuteFamilyNotValid() throws ArcException {
		JSONObject clientJsonInput = new JSONObject(
				"{\"familleNorme\":\"RESIL\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");
		executeImportStep1(clientJsonInput);
	}
	
	
	@Test
	public void testExecute() throws ArcException {

		// parameters sent by client for step 1
		JSONObject clientJsonInputStep1 = new JSONObject(
				"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");

		// response token will be used by client to invoke step2
		String arcResponseStep1 = executeImportStep1(clientJsonInputStep1);

		testCreateAndDropWsPending();
		testCreateTableNmcl();
		testCreateTableVarMetier();
		testCreateTableTableMetier();
		testCreateTableTableFamille();
		testCreateTableTablePeriodicite();
		
		// parameters sent by client for step 2
		// it use response token provided as response of step1
		JSONObject clientJsonInputStep2 =new JSONObject(
				"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"tableName\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\""+arcResponseStep1+"\",\"environnement\":\"arc_bas1\",\"type\":\"jsonwsp/request\"}");
		
		String arcResponseStep2 = executeImportStep2(clientJsonInputStep2);
		
		// ws info must be the first table to be retrieved
		// token must return name of the table and the ddl of the table
		assertEquals(arcResponseStep1+"_ws_info  client text, timestamp text", arcResponseStep2);
		
		
//		JSONObject clientJsonInputStep1 = new JSONObject(
//				"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");
//		
		
		
	}
	
	private void testCreateAndDropWsPending() throws ArcException {
		
		// check that the parallel thread that create tables drop the table ws_pending

		// it should be done in less than 50 iteration, test data is very little
		int maxIteration = 50;
		int i=0;
		
		while (i<maxIteration && UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_ws_pending"))
		{
			i++;
			UtilitaireDao.get(0).executeImmediate(InitializeQueryTest.c, "SELECT pg_sleep(1);");
		}
		
		assertTrue(i>0);
		assertTrue(i<maxIteration);
	}
	
	private void testCreateTableNmcl() throws ArcException {
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_nmcl_table1"));
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_nmcl_table2"));
	}

	private void testCreateTableVarMetier() throws ArcException {
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_mod_variable_metier"));
	}
	
	private void testCreateTableTableMetier() throws ArcException {
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_mod_table_metier"));
	}
	
	private void testCreateTableTableFamille() throws ArcException {
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_ext_mod_famille"));
	}
	
	private void testCreateTableTablePeriodicite() throws ArcException {
		// table image created should be like arc_bas1.ARTEMIS_timestamp_<tablename_to_retrieve>
		assertTrue(UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, "arc_bas1.ARTEMIS_%_ext_mod_periodicite"));
	}

}
