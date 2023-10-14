package fr.insee.arc.ws.services.importServlet;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.bo.JsonKeys;
import fr.insee.arc.ws.services.importServlet.dao.ClientDao;
import fr.insee.arc.ws.services.importServlet.dao.NameDao;

public class ImportStep2GetTableNameService {

	protected static final Logger LOGGER = LogManager.getLogger(ImportStep2GetTableNameService.class);

	private ClientDao clientDao;
	private JSONObject dsnRequest;

	private ArcClientIdentifier arcClientIdentifier;

	private boolean reprise;

	public ImportStep2GetTableNameService(JSONObject dsnRequest) {
		super();

		this.dsnRequest = dsnRequest;

		this.arcClientIdentifier = new ArcClientIdentifier(dsnRequest);

		reprise = this.dsnRequest.getBoolean(JsonKeys.REPRISE.getKey());

		clientDao = new ClientDao(arcClientIdentifier);

	}

	public void execute(SendResponse resp) throws ArcException {

		StringBuilder type = new StringBuilder();

		String tableName = this.clientDao.getAClientTable();

		if (tableName == null) {
			tableName = this.clientDao.getIdTable();

			if (!reprise) {
				this.clientDao.updatePilotage(tableName);
			}

			this.clientDao.dropTable(tableName);

			resp.send(" ");
			resp.endSending();
			return;

		} else {
			// récupération du type
			List<List<String>> metadataOnlyTable = NameDao.execQuerySelectMetadata(tableName);

			for (int j = 0; j < metadataOnlyTable.get(0).size(); j++) {
				if (j > 0) {
					type.append(",");
				}

				for (int i = 0; i < metadataOnlyTable.size(); i++) {
					type.append(" " + metadataOnlyTable.get(i).get(j));
				}
			}
		}

		// renvoie un nom de table du client si il en reste une
		resp.send(tableName + " " + type);
		resp.endSending();
	}

}
