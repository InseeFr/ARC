package fr.insee.arc.ws.services;

import org.json.JSONObject;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.JsonKeys;
import fr.insee.arc.ws.actions.SendResponse;
import fr.insee.arc.ws.dao.ClientDao;
import fr.insee.arc.ws.dao.ClientDaoImpl;

public class ImportStep3GetTableDataService {

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

	public void execute(SendResponse resp) {

		try {
			client = this.dsnRequest.getString(JsonKeys.CLIENT.getKey());

			// binary transfer
			UtilitaireDao.get("arc").exporting(null, client, resp.getWr(), false);
			this.clientDao.dropTable(client);

			resp.endSending();

			// renvoie un nom de table du client si il en reste une

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resp.send("\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
			resp.endSending();
		}
	}

}
