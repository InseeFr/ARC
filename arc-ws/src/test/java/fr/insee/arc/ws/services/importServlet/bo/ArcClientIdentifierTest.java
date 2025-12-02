package fr.insee.arc.ws.services.importServlet.bo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.exception.ArcException;

public class ArcClientIdentifierTest {

	@Test
	public void testArcClientIdentifierInitializeClient() throws ArcException {

		JSONObject json = new JSONObject(
				"{\"service\":\"arcClient\", \"client\":\"ARTEMIS\",\"environnement\":\"arc.bas1\",\"familleNorme\":\"DSN\",\"format\":\"csv_gzip\"}");
		ArcClientIdentifierUnsafe clientParameters = new ArcClientIdentifierUnsafe(json);

		assertEquals("ARTEMIS", clientParameters.getClientInputParameterUnsafe());
		assertEquals("ARTEMIS", clientParameters.getClientIdentifierUnsafe());
		assertEquals("arc_bas1", clientParameters.getEnvironnementUnsafe());
		assertEquals("DSN", clientParameters.getFamilleUnsafe());
		assertEquals(ExportFormat.CSV_GZIP.toString(), clientParameters.getFormatUnsafe().toUpperCase());
	}

	@Test
	public void testArcClientIdentifierRetrieveClientAttributes() throws ArcException {

		JSONObject json = new JSONObject(
				"{\"service\":\"tableContent\",\"client\":\"arc_bas1.ARTEMIS_1701335653112_nmcl_code_pays_etranger_2015\",\"environnement\":\"arc.bas1\",\"familleNorme\":\"DSN\",\"format\":\"csv_gzip\"}");

		ArcClientIdentifierUnsafe clientParameters = new ArcClientIdentifierUnsafe(json);

		assertEquals("arc_bas1.ARTEMIS_1701335653112_nmcl_code_pays_etranger_2015",
				clientParameters.getClientInputParameterUnsafe());
		assertEquals("ARTEMIS", clientParameters.getClientIdentifierUnsafe());
		assertEquals(1701335653112L, clientParameters.getTimestampUnsafe());
		assertEquals("arc_bas1", clientParameters.getEnvironnementUnsafe());
		assertEquals("DSN", clientParameters.getFamilleUnsafe());
		assertEquals(ExportFormat.CSV_GZIP.toString(), clientParameters.getFormatUnsafe().toUpperCase());

	}

}
