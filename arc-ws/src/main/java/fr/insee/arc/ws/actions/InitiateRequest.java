package fr.insee.arc.ws.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.JsonKeys;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.Services;
import fr.insee.arc.ws.services.importServlet.ImportStep1InitializeClientTablesService;
import fr.insee.arc.ws.services.importServlet.ImportStep2GetTableNameService;
import fr.insee.arc.ws.services.importServlet.ImportStep3GetTableDataService;

/**
 * Cette classe permet d'initier le requêtage auprès de la base de données.
 *
 * @author N6YF91
 *
 */
public class InitiateRequest implements IDbConstant {

	protected static final Logger LOGGER = LogManager.getLogger(InitiateRequest.class);
	private JSONObject dsnRequest;

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
	public InitiateRequest(JSONObject dsnRequest) {
		this.dsnRequest = dsnRequest;
	}

	/**
	 * Initie le requêtage en faisant appel au DAO.
	 *
	 * @param resp Le flux dans lequel on écrit la réponse.
	 * @throws ArcException 
	 */
	public void doRequest(SendResponse resp) throws ArcException {
		LoggerHelper.debugDebutMethodeAsComment(getClass(), "doRequest()", LOGGER);

		if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.CLIENT.getService())) {
			new ImportStep1InitializeClientTablesService(dsnRequest).buildParam().execute(resp);
		} else if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.TABLE_NAME.getService())) {
			new ImportStep2GetTableNameService(dsnRequest).buildParam().execute(resp);
		} else if (dsnRequest.getString(JsonKeys.SERVICE.getKey()).equals(Services.TABLE_CONTENT.getService())) {
			new ImportStep3GetTableDataService(dsnRequest).buildParam().execute(resp);
		}
		else {
			resp.send("\"type\":\"jsonwsp/response\",\"error\":\"Le service n'est pas reconnu.\"}");
			resp.endSending();
			throw new ArcException("Le JSON n'est pas conforme");
		}
	}
}
