package fr.insee.arc.ws.services.importServlet;

import java.util.ArrayList;

import org.json.JSONObject;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.JsonKeys;
import fr.insee.arc.ws.actions.SendResponse;
import fr.insee.arc.ws.dao.ClientDao;
import fr.insee.arc.ws.dao.ClientDaoImpl;

public class ImportStep2GetTableNameService {

	private ClientDao clientDao;
	private JSONObject dsnRequest;

	public ImportStep2GetTableNameService(JSONObject dsnRequest) {
		super();
		clientDao = new ClientDaoImpl();
		this.dsnRequest = dsnRequest;
	}

	private long timestamp;

	private String environnement;

	private String client;

	private boolean reprise;

	public ImportStep2GetTableNameService buildParam() {
		timestamp = System.currentTimeMillis();

		environnement = dsnRequest.getString(JsonKeys.ENVIRONNEMENT.getKey());

		client = dsnRequest.getString(JsonKeys.CLIENT.getKey());

		reprise = this.dsnRequest.getBoolean(JsonKeys.REPRISE.getKey());

		return this;
	}

	public void execute(SendResponse resp) {

		try {
			StringBuilder type = new StringBuilder();

			client = this.dsnRequest.getString(JsonKeys.CLIENT.getKey());
			environnement = this.dsnRequest.getString(JsonKeys.ENVIRONNEMENT.getKey());

			String tableName = this.clientDao.getAClientTable(client);

			if (tableName == null) {
				tableName = this.clientDao.getIdTable(client);

				if (!reprise) {
					this.clientDao.updatePilotage(this.timestamp, environnement, tableName);
				}

				this.clientDao.dropTable(tableName);
				tableName = "";
			} else {
				// récupération du type
				ArrayList<ArrayList<String>> l = UtilitaireDao.get("arc").executeRequest(null,
						new PreparedStatementBuilder("select * from " + tableName + " where false "));

				for (int j = 0; j < l.get(0).size(); j++) {
					if (j > 0) {
						type.append(",");
					}

					for (int i = 0; i < l.size(); i++) {
						type.append(" " + l.get(i).get(j));
					}
				}
			}

			// renvoie un nom de table du client si il en reste une
			resp.send(tableName + " " + type);
			resp.endSending();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resp.send("\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
			resp.endSending();
		}
	}

}
