package fr.insee.arc.ws.services.importServlet.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.insee.arc.core.service.global.util.Patch;
import fr.insee.arc.utils.database.Delimiters;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class ArcClientIdentifierUnsafe {

	public ArcClientIdentifierUnsafe(JSONObject dsnRequest) throws ArcException {
		
		// service validation
		try {
			this.serviceSafe = ServletService.valueOf(dsnRequest.getString(JsonKeys.SERVICE.getKey()).toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ArcException(ArcExceptionMessage.JSON_PARSING_FAILED);
		}

		this.dsnRequestUnsafe = dsnRequest;
		this.clientInputParameterUnsafe = dsnRequest.getString(JsonKeys.CLIENT.getKey());
		
		switch(this.serviceSafe) {
			case ARCCLIENT:
				// for service arc client, timestamp must be generated
				this.clientIdentifierUnsafe = this.clientInputParameterUnsafe;
				this.timestampUnsafe = System.currentTimeMillis();
				this.environnementUnsafe = getKeyIfExists(JsonKeys.ENVIRONNEMENT, Patch::normalizeSchemaName);
				break;
			case TABLENAME, TABLECONTENT:
				// for other services, timestamp and other tokens must be retrieved from input parameters
				// as example : arc_bas1.ARTEMIS_1701299079078
				String[] tokens = this.clientInputParameterUnsafe.split("\\"+Delimiters.SQL_SCHEMA_DELIMITER);
				this.environnementUnsafe = tokens[0];
				this.clientIdentifierUnsafe = tokens[1];
				tokens = this.clientIdentifierUnsafe.split("\\"+Delimiters.SQL_TOKEN_DELIMITER);
				this.clientIdentifierUnsafe = tokens[0];
				this.timestampUnsafe = Long.parseLong(tokens[1]);
				break;
			default:
				throw new ArcException(ArcExceptionMessage.JSON_PARSING_FAILED);
		}
		
		this.familleUnsafe = getStringFromKeyIfExists(JsonKeys.FAMILLE);
		this.formatUnsafe = getStringFromKeyIfExists(JsonKeys.FORMAT);
		this.sourceUnsafe = getArrayFromKeyIfExists(JsonKeys.SOURCE);
		this.mappingTablesFilterUnsafe = getArrayFromKeyIfExists(JsonKeys.MAPPING_TABLES_FILTER);

		this.repriseUnsafe = getBooleanFromKeyIfExists(JsonKeys.REPRISE);
		this.validiteInfUnsafe = getStringFromKeyIfExists(JsonKeys.VALINF);
		this.validiteSupUnsafe = getStringFromKeyIfExists(JsonKeys.VALSUP);
		
		
	}

	private ServletService serviceSafe;
	
	private JSONObject dsnRequestUnsafe;
	
	private String clientInputParameterUnsafe;

	private long timestampUnsafe;

	private String environnementUnsafe;

	private String clientIdentifierUnsafe;

	private String familleUnsafe;

	private String formatUnsafe;
	
	private List<String> sourceUnsafe;
	private List<String> mappingTablesFilterUnsafe;

	private Boolean repriseUnsafe;

	private String validiteInfUnsafe;

	private String validiteSupUnsafe;


	
	private List<String> getArrayFromKeyIfExists(JsonKeys key) {
		if (!dsnRequestUnsafe.keySet().contains(key.getKey()))
		{
			return Collections.emptyList();
		}
		
		JSONArray source = dsnRequestUnsafe.getJSONArray(key.getKey());
		List<String> returned = new ArrayList<>();
		for (int i = 0; i < source.length(); i++) {
			returned.add(source.getString(i));
		}
		
		return returned;
	}
	
	private Boolean getBooleanFromKeyIfExists(JsonKeys key) {
		return dsnRequestUnsafe.keySet().contains(key.getKey()) ? dsnRequestUnsafe.getBoolean(key.getKey()) : null;
	}
	
	
	private String getKeyIfExists(JsonKeys key, UnaryOperator<String> f) {
		return dsnRequestUnsafe.keySet().contains(key.getKey()) ? f.apply(dsnRequestUnsafe.getString(key.getKey())) : null;
	}

	private String getStringFromKeyIfExists(JsonKeys key) {
		return getKeyIfExists(key, t -> t);
	}

	public long getTimestampUnsafe() {
		return timestampUnsafe;
	}

	public String getEnvironnementUnsafe() {
		return environnementUnsafe;
	}

	public String getClientIdentifierUnsafe() {
		return clientIdentifierUnsafe;
	}

	public String getFamilleUnsafe() {
		return familleUnsafe;
	}

	public String getFormatUnsafe() {
		return formatUnsafe;
	}

	public String getClientInputParameterUnsafe() {
		return clientInputParameterUnsafe;
	}

	public List<String> getSourceUnsafe() {
		return sourceUnsafe;
	}

	
	public List<String> getMappingTablesFilterUnsafe() {
		return mappingTablesFilterUnsafe;
	}

	public ServletService getServiceSafe() {
		return serviceSafe;
	}

	public Boolean getRepriseUnsafe() {
		return repriseUnsafe;
	}

	public String getValiditeInfUnsafe() {
		return validiteInfUnsafe;
	}

	public String getValiditeSupUnsafe() {
		return validiteSupUnsafe;
	}
	
	
}
