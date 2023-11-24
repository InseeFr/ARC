package fr.insee.arc.ws.services.importServlet.bo;

import java.util.function.UnaryOperator;

import org.json.JSONObject;

import fr.insee.arc.core.service.global.util.Patch;

public class ArcClientIdentifier {

	
	public ArcClientIdentifier(JSONObject dsnRequest) {
		
		this.dsnRequest = dsnRequest;
		this.client = getKeyIfExists(JsonKeys.CLIENT);
		this.timestamp = System.currentTimeMillis();
		this.environnement = getKeyIfExists(JsonKeys.ENVIRONNEMENT, Patch::normalizeSchemaName);
		this.famille = getKeyIfExists(JsonKeys.FAMILLE);
		this.format = getKeyIfExists(JsonKeys.FORMAT);
	}

	private JSONObject dsnRequest;
	
	private long timestamp;

	private String environnement;

	private String client;

	private String famille;
	
	private String format;


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

	public String getEnvironnement() {
		return environnement;
	}


	public String getClient() {
		return client;
	}

	public String getFamille() {
		return famille;
	}

	public String getFormat() {
		return format;
	}
	
}
