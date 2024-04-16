package fr.insee.arc.ws.services.importServlet.actions;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.ws.services.importServlet.ImportStep1InitializeClientTablesService;
import fr.insee.arc.ws.services.importServlet.ImportStep2GetTableNameService;
import fr.insee.arc.ws.services.importServlet.ImportStep3GetTableDataService;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifierUnsafe;
import fr.insee.arc.ws.services.importServlet.bo.ExportFormat;
import fr.insee.arc.ws.services.importServlet.bo.ExportSource;
import fr.insee.arc.ws.services.importServlet.bo.JsonKeys;
import fr.insee.arc.ws.services.importServlet.bo.RemoteHost;
import fr.insee.arc.ws.services.importServlet.bo.ServletService;
import fr.insee.arc.ws.services.importServlet.dao.SecurityDao;

/**
 * Cette classe permet d'initier le requêtage auprès de la base de données.
 *
 * @author N6YF91
 *
 */
public class InitiateRequest {

	protected static final Logger LOGGER = LogManager.getLogger(InitiateRequest.class);
	
	private JSONObject dsnRequest;
	
	private ArcClientIdentifier arcClientIdentifier;

	private RemoteHost remoteHost;
	

	/**
	 * Identifie le service solicité et préparation pour le traitement.
	 *
	 * Voici les formes des JSON reçus pour atteindre un des deux services : <br/>
	 * - service "query": { "type":"jsonwsp/request", "client":"string",
	 * "service":"query", "requests": [ { "id":"string", "sql":"string" }, ] } <br/>
	 * - service "arcClient": { "type":"jsonwsp/request",
	 *
	 * "client":"string", "service":"arcClient", "reprise":"boolean",
	 * "environnement":"string", "familleNorme":"string", "validiteInf":"string",
	 * "validiteSup":"string", "periodicite":"string" } <br/>
	 *
	 * @param queryDao   Objet responsable d'obtenir le données auprès de la base de
	 *                   donnée pour le service query.
	 * @param clientDao  Objet responsable d'obtenir le données auprès de la base de
	 *                   donnée pour le service arcClient.
	 * @param dsnRequest Le JSON contenant les paramètres de la requête.
	 */
	public InitiateRequest(JSONObject dsnRequest, RemoteHost remoteHost) {
		
		LoggerHelper.info(LOGGER, "ServletArc.doPost(): Requête reçue : " + dsnRequest);
		
		this.dsnRequest = dsnRequest;
		
		this.remoteHost = remoteHost;
	}

	/**
	 * Initie le requêtage en faisant appel au DAO.
	 *
	 * @param resp Le flux dans lequel on écrit la réponse.
	 * @throws ArcException 
	 */
	public void doRequest(SendResponse resp) throws ArcException {
		LoggerHelper.debugDebutMethodeAsComment(getClass(), "doRequest()", LOGGER);

		this.arcClientIdentifier = new ArcClientIdentifier(new ArcClientIdentifierUnsafe(dsnRequest), remoteHost);

		switch(this.arcClientIdentifier.getService()) {
			case ARCCLIENT : new ImportStep1InitializeClientTablesService(this.arcClientIdentifier).execute(resp); break;
			case TABLENAME : new ImportStep2GetTableNameService(this.arcClientIdentifier).execute(resp); break;
			case TABLECONTENT : new ImportStep3GetTableDataService(this.arcClientIdentifier).execute(resp); break;
			default: throw new ArcException(ArcExceptionMessage.JSON_PARSING_FAILED);
		}
	}
	
	
}
