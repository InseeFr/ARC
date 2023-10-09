package fr.insee.arc.ws.services.importServlet.bo;

import java.util.function.UnaryOperator;

import org.json.JSONObject;

import fr.insee.arc.core.model.Delimiters;
import fr.insee.arc.core.service.global.util.Patch;
import fr.insee.arc.utils.utils.ManipString;

public class ArcClientIdentifier {

	
	public ArcClientIdentifier(JSONObject dsnRequest) {
		
		this.dsnRequest = dsnRequest;
		
		this.client = getKeyIfExists(JsonKeys.CLIENT, t -> { return ManipString.substringAfterLast(t, Delimiters.HANDSHAKE_DELIMITER); });
		this.timestamp = System.currentTimeMillis();
		this.environnement = getKeyIfExists(JsonKeys.ENVIRONNEMENT, Patch::normalizeSchemaName);
		this.famille = getKeyIfExists(JsonKeys.FAMILLE);
		
	}

	private JSONObject dsnRequest;
	
	private long timestamp;

	private String environnement;

	private String client;

	private String famille;

	private String getKeyIfExists(JsonKeys key, UnaryOperator<String> f )
	{
		return dsnRequest.keySet().contains(key.getKey())?f.apply(dsnRequest.getString(key.getKey())):null;
	}
	
	private String getKeyIfExists(JsonKeys key)
	{
		return getKeyIfExists(key, t -> t );
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getEnvironnement() {
		return environnement;
	}

	public void setEnvironnement(String environnement) {
		this.environnement = environnement;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getFamille() {
		return famille;
	}

	public void setFamille(String famille) {
		this.famille = famille;
	}
	

	
	
}