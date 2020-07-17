package fr.insee.arc.ws.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import fr.insee.arc.utils.utils.JsonKeys;
import fr.insee.arc.ws.actions.SendResponse;
import fr.insee.arc.ws.dao.DAOException;
import fr.insee.arc.ws.dao.QueryDao;
import fr.insee.arc.ws.dao.QueryDaoImpl;

public class GetQueryResultService {

 private QueryDao queryDao;
 private JSONObject dsnRequest;

	
/**
 * Build object and parameters for the service
 * @param dsnRequest
 */
public GetQueryResultService(JSONObject dsnRequest) {
		super();
		this.dsnRequest=dsnRequest;
		this.queryDao = new QueryDaoImpl();
	}

private long timestamp;

private List<String> ids;

private HashMap<String, String> sqlRequests;


public GetQueryResultService buildParam()
{
	timestamp = System.currentTimeMillis();

	parseRequests(this.dsnRequest);
	
	return this;
}

	
public void execute(SendResponse resp)
{
	 try {

		 queryDao.createImage(this.ids, this.sqlRequests, this.timestamp);
         resp.send("{\"type\":\"jsonwsp/response\",\"responses\":[");
         int i = 0;
         for (String id : this.ids) {
             i++;
             resp.send("{\"" + JsonKeys.ID.getKey() + "\":\"");
             for (int j = 3; j < id.split("_").length; j++) {
                 if (j != 3) {
                     resp.send("_");
                 }
                 resp.send(id.split("_")[j]);
             }
             resp.send("\",\"" + JsonKeys.TABLE.getKey() + "\":");
             queryDao.doRequest(id, resp, this.timestamp);
             resp.send("}");
             if (this.ids.size() != i) {
                 resp.send(",");
             }
         }
         resp.send("]}");
         resp.endSending();
     } catch (DAOException e) {
         resp.send("{\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
         resp.endSending();
     }
}



/**
 * Cette fonction permet de parser les requêtes sql contenue dans le fichier JSON reçu pour le service QUERY
 *
 * @param dsnRequest
 */
private boolean parseRequests(JSONObject dsnRequest) {
	
	this.ids = new ArrayList<String>();
    this.sqlRequests = new HashMap<String, String>();
	
    JSONObject sqlRequest = new JSONObject();
    for (int i = 0; i < dsnRequest.getJSONArray(JsonKeys.REQUESTS.getKey()).length(); i++) {
        sqlRequest = dsnRequest.getJSONArray(JsonKeys.REQUESTS.getKey()).getJSONObject(i);
        if (this.sqlRequests.containsKey(sqlRequest.getString(JsonKeys.ID.getKey())) == false) {
            if (sqlRequest.getString(JsonKeys.ID.getKey()) != "") {
                this.ids.add("temp_" + dsnRequest.getString(JsonKeys.CLIENT.getKey()) + "_" + this.timestamp + "_"
                        + sqlRequest.getString(JsonKeys.ID.getKey()));
                this.sqlRequests.put(
                        "temp_" + dsnRequest.getString(JsonKeys.CLIENT.getKey()) + "_" + this.timestamp + "_"
                                + sqlRequest.getString(JsonKeys.ID.getKey()), sqlRequest.getString(JsonKeys.SQL.getKey()));
            } else {
                this.ids.add("temp_" + dsnRequest.getString(JsonKeys.CLIENT.getKey()) + "_" + this.timestamp + "_r" + i);
                this.sqlRequests.put("temp_" + dsnRequest.getString(JsonKeys.CLIENT.getKey()) + "_" + this.timestamp + "_r" + i,
                        sqlRequest.getString(JsonKeys.SQL.getKey()));
            }
        } else {
            throw new DAOException("Id présent plusieurs fois : " + sqlRequest.getString(JsonKeys.ID.getKey()));
        }
    }
    return true;
}
	
}
