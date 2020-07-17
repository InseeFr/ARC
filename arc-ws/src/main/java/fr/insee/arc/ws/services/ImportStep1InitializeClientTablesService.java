package fr.insee.arc.ws.services;

import java.util.ArrayList;

import org.json.JSONObject;

import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.utils.utils.JsonKeys;
import fr.insee.arc.ws.actions.SendResponse;
import fr.insee.arc.ws.dao.ClientDao;
import fr.insee.arc.ws.dao.ClientDaoImpl;
import fr.insee.arc.ws.dao.DAOException;

public class ImportStep1InitializeClientTablesService {

	private ClientDao clientDao;
	private JSONObject dsnRequest;

	public ImportStep1InitializeClientTablesService(JSONObject dsnRequest) {
		super();
		clientDao = new ClientDaoImpl();
		this.dsnRequest = dsnRequest;
	}

	private long timestamp;

	private String environnement;

	private String client;

	private String famille;

	private ArrayList<ArrayList<String>> tablesMetierNames;

	

public ImportStep1InitializeClientTablesService buildParam()
{
	timestamp = System.currentTimeMillis();

	environnement = dsnRequest.getString(JsonKeys.ENVIRONNEMENT.getKey());

	client = dsnRequest.getString(JsonKeys.CLIENT.getKey());
	
	famille = dsnRequest.getString(JsonKeys.FAMILLE.getKey());
	
	this.tablesMetierNames = this.clientDao.getIdSrcTableMetier(this.timestamp,
			dsnRequest);
	
	return this;
}

	
	
	public void execute(SendResponse resp) {

		try {

			if (!environnement.equalsIgnoreCase("arc")) {
				this.clientDao.verificationClientFamille(this.timestamp, this.client, this.famille, this.environnement);
				tablesMetierNames = this.clientDao.getIdSrcTableMetier(this.timestamp, this.dsnRequest);
				this.clientDao.createImages(this.timestamp, client, environnement, tablesMetierNames);
				this.clientDao.createTableMetier(this.timestamp, client, this.famille, environnement);
				this.clientDao.createVarMetier(this.timestamp, client, this.famille, environnement);
			}
			this.clientDao.createNmcl(this.timestamp, client, environnement);
			this.clientDao.createTableFamille(this.timestamp, client, environnement);
			this.clientDao.createTablePeriodicite(this.timestamp, client, environnement);
			// on renvoie l'id du client avec son timestamp
			resp.send(ApiService.dbEnv(environnement) + client + "_" + this.timestamp);
			resp.endSending();

		} catch (DAOException e) {
			e.printStackTrace();
			resp.send("\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
			resp.endSending();
		}

	}

}
