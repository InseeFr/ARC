package fr.insee.arc.ws.services.importServlet;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifierUnsafe;
import fr.insee.arc.ws.services.importServlet.bo.ExportTrackingType;
import fr.insee.arc.ws.services.importServlet.dao.InitializeTestDataNoScalability;

public class ImportKOTest extends ServletArc {

	private static final long serialVersionUID = 4409305598494746785L;



	@BeforeClass
    public static void setup() throws SQLException, ArcException {
		InitializeTestDataNoScalability.destroyTestData();
		InitializeTestDataNoScalability.initializeTestData(false);
	}
	
	@AfterClass
    public static void tearDown() throws SQLException, ArcException {
		InitializeTestDataNoScalability.destroyTestData();
    }

	private String executeImportStep1(JSONObject clientJsonInput) throws ArcException
	{
		ArcClientIdentifier clientJsonInputValidated= new ArcClientIdentifier(new ArcClientIdentifierUnsafe(clientJsonInput), null);
		ImportStep1InitializeClientTablesService imp = new ImportStep1InitializeClientTablesService(clientJsonInputValidated);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		SendResponse sentResponse = new SendResponse(bos);
		imp.execute(sentResponse);
		return sentResponse.getWr().toString();
	}
	
	
	@Test
	public void testExecute() throws ArcException {

		JSONObject clientJsonInput = new JSONObject(
				"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");

		String arcResponse = executeImportStep1(clientJsonInput);

		testCreateAndDropWsPending(arcResponse);
		
		testRegisterWsKo(arcResponse);
		
	}
	
	private void testCreateAndDropWsPending(String arcResponse) throws ArcException {
		
		// check that the parallel thread that create tables drop the table ws_pending

		// it should be done in less than 50 iteration, test data is very little
		int maxIteration = 50;
		int i=0;
		
		while (i<maxIteration && UtilitaireDao.get(0).isTableExiste(InitializeQueryTest.c, arcResponse+"_ws_pending"))
		{
			i++;
			UtilitaireDao.get(0).executeImmediate(InitializeQueryTest.c, "SELECT pg_sleep(1);");
		}
		
		assertTrue(i>0);
		assertTrue(i<maxIteration);
	}
	
	private void testRegisterWsKo(String arcResponse) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		query.append("SELECT 1 FROM "+arcResponse+"_ws_tracking where tracking_type="+query.quoteText(ExportTrackingType.KO.toString()));
		
		UtilitaireDao.get(0).hasResults(InitializeQueryTest.c, query);
	}

}
