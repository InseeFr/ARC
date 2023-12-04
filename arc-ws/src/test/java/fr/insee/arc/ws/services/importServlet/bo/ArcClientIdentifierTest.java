package fr.insee.arc.ws.services.importServlet.bo;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

public class ArcClientIdentifierTest {

	@Test
	public void testArcClientIdentifierInitializeClient() {
		
		JSONObject json = new JSONObject("{\"client\":\"ARTEMIS\",\"environnement\":\"arc.bas1\",\"familleNorme\":\"DSN\",\"format\":\"csv_gzip\"}");		
		ArcClientIdentifier clientParameters = new ArcClientIdentifier(json, true);
		
		assertEquals("ARTEMIS",clientParameters.getClientInputParameter());
		assertEquals("ARTEMIS",clientParameters.getClientIdentifier());
		assertEquals("arc_bas1",clientParameters.getEnvironnement());
		assertEquals("DSN",clientParameters.getFamille());
		assertEquals(ExportFormat.CSV_GZIP.getFormat(),clientParameters.getFormat());
	}
	
	
	@Test
	public void testArcClientIdentifierRetrieveClientAttributes() {
		
		JSONObject json = new JSONObject("{\"client\":\"arc_bas1.ARTEMIS_1701335653112_nmcl_code_pays_etranger_2015\",\"environnement\":\"arc.bas1\",\"familleNorme\":\"DSN\",\"format\":\"csv_gzip\"}");
		
		ArcClientIdentifier clientParameters = new ArcClientIdentifier(json, false);

		assertEquals("arc_bas1.ARTEMIS_1701335653112_nmcl_code_pays_etranger_2015",clientParameters.getClientInputParameter());
		assertEquals("ARTEMIS",clientParameters.getClientIdentifier());
		assertEquals(1701335653112L,clientParameters.getTimestamp());
		assertEquals("arc_bas1",clientParameters.getEnvironnement());
		assertEquals("DSN",clientParameters.getFamille());
		assertEquals(ExportFormat.CSV_GZIP.getFormat(),clientParameters.getFormat());
		
	}

}
