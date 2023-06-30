package fr.insee.arc.ws.services.importServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.JsonKeys;
import fr.insee.arc.ws.actions.SendResponse;
import fr.insee.arc.ws.dao.ClientDao;
import fr.insee.arc.ws.dao.ClientDaoImpl;

public class ImportStep3GetTableDataService {
	
	protected static final Logger LOGGER = LogManager.getLogger(ImportStep3GetTableDataService.class);


	private ClientDao clientDao;
	private JSONObject dsnRequest;

	public ImportStep3GetTableDataService(JSONObject dsnRequest) {
		super();
		clientDao = new ClientDaoImpl();
		this.dsnRequest = dsnRequest;
	}

	private String client;

	public ImportStep3GetTableDataService buildParam() {

		client = dsnRequest.getString(JsonKeys.CLIENT.getKey());

		return this;
	}

	public void execute(SendResponse resp) throws ArcException {

		try {
			client = this.dsnRequest.getString(JsonKeys.CLIENT.getKey());

			// binary transfer
			UtilitaireDao.get(0).exporting(null, client, resp.getWr(), false);
			this.clientDao.dropTable(client);

			resp.endSending();

			// renvoie un nom de table du client si il en reste une

		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, "** Error in servlet ImportStep3GetTableDataService **");
			resp.send("\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
			resp.endSending();
		}
	}

}
