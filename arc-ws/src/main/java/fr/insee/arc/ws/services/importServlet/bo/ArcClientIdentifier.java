package fr.insee.arc.ws.services.importServlet.bo;

import org.apache.commons.lang3.RegExUtils;

public class ArcClientIdentifier {


	public ArcClientIdentifier(ArcClientIdentifierUnsafe unsafe) {
		this.clientInputParameter = RegExUtils.replacePattern(unsafe.getClientInputParameter(), "[^\\w\\.]*", "");
		this.clientIdentifier = RegExUtils.replacePattern(unsafe.getClientIdentifier(), "[^\\w\\.]*", "");
		this.timestamp = unsafe.getTimestamp();
		this.environnement = RegExUtils.replacePattern(unsafe.getEnvironnement(), "[^\\w\\.]*", "");
		this.famille = RegExUtils.replacePattern(unsafe.getFamille(), "[^\\w\\.]*", "");
		this.format = unsafe.getFormat();
	}

	
	private String clientInputParameter;

	private long timestamp;

	private String environnement;

	private String clientIdentifier;

	private String famille;

	private String format;

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
