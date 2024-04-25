package fr.insee.arc.ws.services.importServlet.bo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.insee.arc.utils.database.Delimiters;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.security.SecurityDao;
import fr.insee.arc.ws.services.importServlet.dao.WsSecurityDao;

public class ArcClientIdentifier {

	private static final List<ExportSource> DEFAULT_SOURCE = Arrays.asList(ExportSource.MAPPING,
			ExportSource.NOMENCLATURE, ExportSource.METADATA);

	ArcClientIdentifierUnsafe unsafe;
	
	private ServletService service;
	
	private String clientInputParameter;

	private long timestamp;

	private String environnement;

	private String clientIdentifier;

	private String famille;

	private ExportFormat format;
	
	private List<ExportSource> source;
	
	private Boolean reprise;

	private String validityInf;

	private String validitySup;

	private String sessionId;
	

	public ArcClientIdentifier(ArcClientIdentifierUnsafe rawParameters, RemoteHost remoteHost) throws ArcException {
		
		this.unsafe = rawParameters;
		this.service = rawParameters.getServiceSafe();
		
		validateFormat();
		validateSource();
		this.clientInputParameter = rawParameters.getClientInputParameterUnsafe();

		this.environnement = SecurityDao.validateEnvironnement(rawParameters.getEnvironnementUnsafe());
		this.clientIdentifier = WsSecurityDao.validateClientIdentifier(rawParameters.getClientIdentifierUnsafe());
		this.timestamp = rawParameters.getTimestampUnsafe();
		this.famille = rawParameters.getFamilleUnsafe();
		
		this.reprise = rawParameters.getRepriseUnsafe();
		this.validityInf = rawParameters.getValiditeInfUnsafe();
		this.validitySup = rawParameters.getValiditeSupUnsafe();
		
		if (remoteHost!=null)
		{
			WsSecurityDao.securityAccessAndTracing(this.famille, this.clientIdentifier, remoteHost);
		}
		
		this.sessionId = this.environnement + Delimiters.SQL_SCHEMA_DELIMITER + this.clientIdentifier + Delimiters.SQL_TOKEN_DELIMITER
		+ this.timestamp;
		
	}
	
	
	private void validateSource() throws ArcException {
		this.source = new ArrayList<>();
		if (unsafe.getSourceUnsafe().isEmpty()) {
			this.source.addAll(DEFAULT_SOURCE);
		}
		else
		{
			for (String unsafeSource : unsafe.getSourceUnsafe())
			{
				try {
					this.source.add(ExportSource.valueOf(unsafeSource.toUpperCase()));
				} catch (IllegalArgumentException e) {
					throw new ArcException(ArcExceptionMessage.JSON_PARSING_FAILED);
				}
			}
		}				
	}


	private void validateFormat() throws ArcException {
		if (unsafe.getFormatUnsafe() == null) {
			this.format = ExportFormat.BINARY;
		}
		else
		{
			try {
				this.format = ExportFormat.valueOf(unsafe.getFormatUnsafe().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new ArcException(ArcExceptionMessage.JSON_PARSING_FAILED);
			}
		}
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

	public ExportFormat getFormat() {
		return format;
	}

	public String getClientInputParameter() {
		return clientInputParameter;
	}

	public List<ExportSource> getSource() {
		return source;
	}

	public ServletService getService() {
		return service;
	}

	public ArcClientIdentifierUnsafe getUnsafe() {
		return unsafe;
	}

	public Boolean getReprise() {
		return reprise;
	}

	public String getValidityInf() {
		return validityInf;
	}


	public String getValiditySup() {
		return validitySup;
	}


	public String getSessionId() {
		return sessionId;
	}

	
}
