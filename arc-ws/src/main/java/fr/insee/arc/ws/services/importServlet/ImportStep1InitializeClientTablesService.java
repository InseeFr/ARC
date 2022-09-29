package fr.insee.arc.ws.services.importServlet;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.JsonKeys;
import fr.insee.arc.ws.actions.SendResponse;
import fr.insee.arc.ws.dao.ClientDao;
import fr.insee.arc.ws.dao.ClientDaoImpl;

public class ImportStep1InitializeClientTablesService {

	protected static final Logger LOGGER = LogManager.getLogger(ImportStep1InitializeClientTablesService.class);

    static interface Executable {
        void execute() throws ArcException;
    }
    
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
	
	private List<String> sources;

	private static final String SPECIALENVIRONMENT="arc";

    public ImportStep1InitializeClientTablesService buildParam() throws ArcException
    {
    	timestamp = System.currentTimeMillis();
    
    	environnement = dsnRequest.getString(JsonKeys.ENVIRONNEMENT.getKey());
    
    	client = dsnRequest.getString(JsonKeys.CLIENT.getKey());
    	
    	famille = dsnRequest.getString(JsonKeys.FAMILLE.getKey());
    	
        sources = makeSource();
    	
    	if (!environnement.equalsIgnoreCase(SPECIALENVIRONMENT)) {
    		this.tablesMetierNames = this.clientDao.getIdSrcTableMetier(this.timestamp,
    				dsnRequest);
    	}
    	
    	return this;
    }

	private void executeIf(String source, Executable exe) throws ArcException {
	    if (!sources.contains(source)) {
            return;
        }
	    exe.execute();
	}
	
    private List<String> makeSource() {
        JSONArray source = dsnRequest.getJSONArray(JsonKeys.SOURCE.getKey());
        List<String> returned = new ArrayList<>();
        for (int i = 0; i < source.length(); i++) {
            returned.add(source.getString(i));
        }
        return returned;
    }
    
    public void execute(SendResponse resp) throws ArcException {

        try {

            if (!environnement.equalsIgnoreCase(SPECIALENVIRONMENT)) {
                this.clientDao.verificationClientFamille(this.timestamp, this.client, this.famille, this.environnement);
                tablesMetierNames = this.clientDao.getIdSrcTableMetier(this.timestamp, this.dsnRequest);
                executeIf(ServletArc.MAPPING, () -> this.clientDao.createImages(this.timestamp, client, environnement, tablesMetierNames));
                executeIf(ServletArc.METADATA, () -> this.clientDao.createTableMetier(this.timestamp, client, this.famille, environnement));
                executeIf(ServletArc.METADATA, () -> this.clientDao.createVarMetier(this.timestamp, client, this.famille, environnement));
            }
            executeIf(ServletArc.NOMENCLATURE, () -> this.clientDao.createNmcl(this.timestamp, client, environnement));
            executeIf(ServletArc.METADATA, () -> this.clientDao.createTableFamille(this.timestamp, client, environnement));
            executeIf(ServletArc.METADATA, () -> this.clientDao.createTablePeriodicite(this.timestamp, client, environnement));
            // on renvoie l'id du client avec son timestamp
            resp.send(ApiService.dbEnv(environnement) + client + "_" + this.timestamp);
            resp.endSending();
        } catch (ArcException e) {
			StaticLoggerDispatcher.error("** Error in servlet ImportStep1InitializeClientTablesService **", LOGGER);
            resp.send("\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
            resp.endSending();
        }
    }   

}
