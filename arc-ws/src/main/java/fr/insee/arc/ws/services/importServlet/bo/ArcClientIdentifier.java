package fr.insee.arc.ws.services.importServlet.bo;

import java.util.function.UnaryOperator;

import org.json.JSONObject;

import fr.insee.arc.core.service.global.util.Patch;
import fr.insee.arc.utils.database.Delimiters;

public class ArcClientIdentifier {


	public ArcClientIdentifier(JSONObject dsnRequest, boolean generateTimeStamp) {
		this.dsnRequest = dsnRequest;
		this.clientInputParameter = dsnRequest.getString(JsonKeys.CLIENT.getKey());
		
		if (generateTimeStamp)
		{
			this.clientIdentifier = this.clientInputParameter;
			this.timestamp = System.currentTimeMillis();
			this.environnement = getKeyIfExists(JsonKeys.ENVIRONNEMENT, Patch::normalizeSchemaName);
		}
		else
		{
			// as example : arc_bas1.ARTEMIS_1701299079078
			String[] tokens = this.clientInputParameter.split("\\"+Delimiters.SQL_SCHEMA_DELIMITER);
			this.environnement = tokens[0];
			this.clientIdentifier = tokens[1];
			tokens = this.clientIdentifier.split("\\"+Delimiters.SQL_TOKEN_DELIMITER);
			this.clientIdentifier = tokens[0];
			this.timestamp = Long.parseLong(tokens[1]);
		}
		this.famille = getKeyIfExists(JsonKeys.FAMILLE);
		this.format = getKeyIfExists(JsonKeys.FORMAT);
	}

	private JSONObject dsnRequest;
	
	private String clientInputParameter;

	private long timestamp;

	private String environnement;

	private String clientIdentifier;

	private String famille;

	private String format;

	private String getKeyIfExists(JsonKeys key, UnaryOperator<String> f) {
		return dsnRequest.keySet().contains(key.getKey()) ? f.apply(dsnRequest.getString(key.getKey())) : null;
	}

	private String getKeyIfExists(JsonKeys key) {
		return getKeyIfExists(key, t -> t);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getEnvironnement() {
		return environnement;
	}

	public String getClientIdentifier() {
		return clientIdentifier;
	}

	public String getFamille() {
		return famille;
	}

	public String getFormat() {
		return format;
	}

	public String getClientInputParameter() {
		return clientInputParameter;
	}

}
