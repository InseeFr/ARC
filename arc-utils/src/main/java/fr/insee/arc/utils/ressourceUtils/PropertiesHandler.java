package fr.insee.arc.utils.ressourceUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.bouncycastle.util.Arrays;
import org.springframework.stereotype.Service;

import fr.insee.arc.utils.consumer.ThrowingFunction;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.kubernetes.provider.KubernetesServiceLayer;
import fr.insee.arc.utils.utils.ManipString;

@Service("properties")
public class PropertiesHandler {

	/* Database */
	private String databasePoolName;
	private String databaseRestrictedUsername;
	private String databaseUrl;
	private String databaseUsername;
	private String databasePassword;
	private String databaseDriverClassName;
	private String databaseSchema;

	/*
	 * List of connection attributes
	 */
	private List<ConnectionAttribute> connectionProperties;

	/* Log */
	private String logDirectory;
	private String logLevel;
	private String logFileName;
	private String logConfiguration;
	/* Batch */
	private String batchParametersDirectory;
	private String batchExecutionEnvironment;
	/* Miscellaneous */
	private String version;
	private String versionDate;
	private String application;
	/* Directories */
	private String registrationDirectory;
	private String loadingDirectory;
	private String storageDirectory;

	private String authorizedRoles;
	private String disableDebugGui;

	private String gitCommitId;
	
	/* Kubernetes */
	private String kubernetesApiUri;
	private String kubernetesApiNamespace;
	private String kubernetesApiTokenValue;
	private String kubernetesApiTokenPath;

	private String kubernetesExecutorImage;
	private int kubernetesExecutorNumber;
	private String kubernetesExecutorLabel;
	private String kubernetesExecutorUser;
	private String kubernetesExecutorDatabase;
	private String kubernetesExecutorPort;
	private String kubernetesExecutorCpu;
	private String kubernetesExecutorRam;
	private String kubernetesExecutorEphemeral;
	private String kubernetesExecutorEphemeralVolumeSize;
	private String kubernetesExecutorTemptablespaceMedium;
	private String kubernetesExecutorVolatile;
	
	/* export data end of batch ? */
	private String processExport;

	private String s3InputApiUri;
	private String s3InputBucket;
	private String s3InputDirectory;
	private String s3InputAccess;
	private String s3InputSecret;
	
	private String s3OutputApiUri;
	private String s3OutputBucket;
	private String s3OutputDirectory;
	private String s3OutputAccess;
	private String s3OutputSecret;
	private String s3OutputParquetKey;

	protected String keycloakRealm;
	private String keycloakServer;
	private String keycloakResource;
	private String keycloakCredential;
	
	private Integer filesRetentionDays;
	
	private static PropertiesHandler instanceOfPropertiesHandler;

	// remap database host address ; can be useful to prevent dns spam for example
	private ThrowingFunction<String, String> remapHostAddress = t -> {return t;};

	
	public void initializeLog() {
		LogConfigurator logConf = new LogConfigurator(logConfiguration);

		// if logDirectory (fr.insee.arc.log.directory) is set
		if (logDirectory != null && !logDirectory.trim().isEmpty()) {
			logConf.configureRollingFileAppender(logDirectory, logFileName);
		}

		if (logLevel != null && !logLevel.trim().isEmpty()) {
			logConf.configureLogLevel(logLevel);
		}
	}

	public static PropertiesHandler getInstance() {
		if (instanceOfPropertiesHandler == null) {
			try {
				instanceOfPropertiesHandler = (PropertiesHandler) SpringApplicationContext.getBean("properties");
			} catch (NullPointerException e) {
				ArcException ex = new ArcException(ArcExceptionMessage.SPRING_BEAN_PROPERTIES_NOTFOUND);
				ex.logMessageException();
				// create a blank instance singleton
				instanceOfPropertiesHandler = new PropertiesHandler();
			}
		}
		return instanceOfPropertiesHandler;
	}

	public String getDatabasePoolName() {
		return databasePoolName;
	}

	public void setDatabasePoolName(String databasePoolName) {
		this.databasePoolName = databasePoolName;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
		// reset the connection getter
		this.connectionProperties=null;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public void setDatabaseUsername(String databaseUsername) {
		this.databaseUsername = databaseUsername;
		// reset the connection getter
		this.connectionProperties=null;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
		// reset the connection getter
		this.connectionProperties=null;
	}

	public String getDatabaseDriverClassName() {
		return databaseDriverClassName;
	}

	public void setDatabaseDriverClassName(String databaseDriverClassName) {
		this.databaseDriverClassName = databaseDriverClassName;
		// reset the connection getter
		this.connectionProperties=null;
	}

	public String getDatabaseSchema() {
		return databaseSchema;
	}

	public void setDatabaseSchema(String databaseSchema) {
		this.databaseSchema = databaseSchema;
	}

	public String getLogDirectory() {
		return logDirectory;
	}

	public void setLogDirectory(String logDirectory) {
		this.logDirectory = logDirectory;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	public String getLogConfiguration() {
		return logConfiguration;
	}

	public void setLogConfiguration(String logConfiguration) {
		this.logConfiguration = logConfiguration;
		initializeLog();
	}

	public String getBatchParametersDirectory() {
		return batchParametersDirectory;
	}

	public void setBatchParametersDirectory(String batchParametersDirectory) {
		this.batchParametersDirectory = batchParametersDirectory;
	}

	public String getBatchExecutionEnvironment() {
		return batchExecutionEnvironment;
	}

	public void setBatchExecutionEnvironment(String batchExecutionEnvironment) {
		this.batchExecutionEnvironment = batchExecutionEnvironment;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = ManipString.substringAfterFirst(version, "version-");
	}

	public String getVersionDate() {
		return versionDate;
	}

	public void setVersionDate(String versionDate) {
		this.versionDate = versionDate;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getRegistrationDirectory() {
		return registrationDirectory;
	}

	public void setRegistrationDirectory(String registrationDirectory) {
		this.registrationDirectory = registrationDirectory;
	}

	public String getLoadingDirectory() {
		return loadingDirectory;
	}

	public void setLoadingDirectory(String loadingDirectory) {
		this.loadingDirectory = loadingDirectory;
	}

	public String getStorageDirectory() {
		return storageDirectory;
	}

	public void setStorageDirectory(String storageDirectory) {
		this.storageDirectory = storageDirectory;
	}

	public void setAuthorizedRoles(String authorizedRoles) {
		this.authorizedRoles = authorizedRoles;
	}

	public String[] getAuthorizedRoles() {
		if (authorizedRoles == null || authorizedRoles.isEmpty()) {
			return new String[0];
		}
		return authorizedRoles.split(",");
	}

	public String getDatabaseRestrictedUsername() {
		return databaseRestrictedUsername;
	}

	public void setDatabaseRestrictedUsername(String databaseRestrictedUsername) {
		this.databaseRestrictedUsername = databaseRestrictedUsername;
	}

	public String getDisableDebugGui() {
		return disableDebugGui;
	}

	public void setDisableDebugGui(String disableDebugGui) {
		this.disableDebugGui = disableDebugGui;
	}

	public String getGitCommitId() {
		return gitCommitId;
	}

	public void setGitCommitId(String gitCommitId) {
		this.gitCommitId = gitCommitId;
	}

	
	public String getKubernetesApiUri() {
		return kubernetesApiUri;
	}

	public void setKubernetesApiUri(String kubernetesApiUri) {
		this.kubernetesApiUri = kubernetesApiUri;
	}
	
	public String getKubernetesApiTokenValue() {
		return kubernetesApiTokenValue;
	}

	public void setKubernetesApiTokenValue(String kubernetesApiTokenValue) {
		this.kubernetesApiTokenValue = kubernetesApiTokenValue;
	}

	public String getKubernetesApiTokenPath() {
		return kubernetesApiTokenPath;
	}

	public void setKubernetesApiTokenPath(String kubernetesApiTokenPath) {
		this.kubernetesApiTokenPath = kubernetesApiTokenPath;
	}

	public String getKubernetesApiNamespace() {
		return kubernetesApiNamespace;
	}

	public void setKubernetesApiNamespace(String kubernetesApiNamespace) {
		this.kubernetesApiNamespace = kubernetesApiNamespace;
	}

	public String getKubernetesExecutorImage() {
		return kubernetesExecutorImage;
	}

	public void setKubernetesExecutorImage(String kubernetesExecutorImage) {
		this.kubernetesExecutorImage = kubernetesExecutorImage;
	}

	public int getKubernetesExecutorNumber() {
		return kubernetesExecutorNumber;
	}

	public void setKubernetesExecutorNumber(int kubernetesExecutorNumber) {
		this.kubernetesExecutorNumber = kubernetesExecutorNumber;
	}

	public String getKubernetesExecutorLabel() {
		return kubernetesExecutorLabel;
	}

	public void setKubernetesExecutorLabel(String kubernetesExecutorLabel) {
		this.kubernetesExecutorLabel = kubernetesExecutorLabel;
	}
	
	public String getKubernetesExecutorUser() {
		return kubernetesExecutorUser;
	}

	public void setKubernetesExecutorUser(String kubernetesExecutorUser) {
		this.kubernetesExecutorUser = kubernetesExecutorUser;
	}

	public String getKubernetesExecutorDatabase() {
		return kubernetesExecutorDatabase;
	}

	public void setKubernetesExecutorDatabase(String kubernetesExecutorDatabase) {
		this.kubernetesExecutorDatabase = kubernetesExecutorDatabase;
	}

	public String getKubernetesExecutorPort() {
		return kubernetesExecutorPort;
	}

	public void setKubernetesExecutorPort(String kubernetesExecutorPort) {
		this.kubernetesExecutorPort = kubernetesExecutorPort;
	}

	public String getKubernetesExecutorCpu() {
		return kubernetesExecutorCpu;
	}

	public void setKubernetesExecutorCpu(String kubernetesExecutorCpu) {
		this.kubernetesExecutorCpu = kubernetesExecutorCpu;
	}

	public String getKubernetesExecutorRam() {
		return kubernetesExecutorRam;
	}

	public void setKubernetesExecutorRam(String kubernetesExecutorRam) {
		this.kubernetesExecutorRam = kubernetesExecutorRam;
	}
	
	public String getKubernetesExecutorEphemeral() {
		return kubernetesExecutorEphemeral;
	}

	public void setKubernetesExecutorEphemeral(String kubernetesExecutorEphemeral) {
		this.kubernetesExecutorEphemeral = kubernetesExecutorEphemeral;
	}

	public String getKubernetesExecutorEphemeralVolumeSize() {
		return kubernetesExecutorEphemeralVolumeSize;
	}

	public void setKubernetesExecutorEphemeralVolumeSize(String kubernetesExecutorEphemeralVolumeSize) {
		this.kubernetesExecutorEphemeralVolumeSize = kubernetesExecutorEphemeralVolumeSize;
	}

	public String getKubernetesExecutorTemptablespaceMedium() {
		return kubernetesExecutorTemptablespaceMedium;
	}

	public void setKubernetesExecutorTemptablespaceMedium(String kubernetesExecutorTemptablespaceMedium) {
		this.kubernetesExecutorTemptablespaceMedium = kubernetesExecutorTemptablespaceMedium;
	}

	public String getKubernetesExecutorVolatile() {
		return kubernetesExecutorVolatile;
	}

	public void setKubernetesExecutorVolatile(String kubernetesExecutorVolatile) {
		this.kubernetesExecutorVolatile = kubernetesExecutorVolatile;
	}
	
	public String getProcessExport() {
		return processExport;
	}

	public void setProcessExport(String processExport) {
		this.processExport = processExport;
	}

	public String getS3InputApiUri() {
		return s3InputApiUri;
	}

	public void setS3InputApiUri(String s3InputApiUri) {
		this.s3InputApiUri = s3InputApiUri;
	}

	public String getS3InputBucket() {
		return s3InputBucket;
	}

	public void setS3InputBucket(String s3InputBucket) {
		this.s3InputBucket = s3InputBucket;
	}

	public String getS3InputDirectory() {
		return s3InputDirectory;
	}

	public void setS3InputDirectory(String s3InputDirectory) {
		this.s3InputDirectory = s3InputDirectory;
	}

	public String getS3InputAccess() {
		return s3InputAccess;
	}

	public void setS3InputAccess(String s3InputAccess) {
		this.s3InputAccess = s3InputAccess;
	}

	public String getS3InputSecret() {
		return s3InputSecret;
	}

	public void setS3InputSecret(String s3InputSecret) {
		this.s3InputSecret = s3InputSecret;
	}

	public String getS3OutputApiUri() {
		return s3OutputApiUri;
	}

	public void setS3OutputApiUri(String s3OutputApiUri) {
		this.s3OutputApiUri = s3OutputApiUri;
	}

	public String getS3OutputBucket() {
		return s3OutputBucket;
	}

	public void setS3OutputBucket(String s3OutputBucket) {
		this.s3OutputBucket = s3OutputBucket;
	}

	public String getS3OutputDirectory() {
		return s3OutputDirectory;
	}

	public void setS3OutputDirectory(String s3OutputDirectory) {
		this.s3OutputDirectory = s3OutputDirectory;
	}

	public String getS3OutputAccess() {
		return s3OutputAccess;
	}

	public void setS3OutputAccess(String s3OutputAccess) {
		this.s3OutputAccess = s3OutputAccess;
	}

	public String getS3OutputSecret() {
		return s3OutputSecret;
	}

	public void setS3OutputSecret(String s3OutputSecret) {
		this.s3OutputSecret = s3OutputSecret;
	}

	public String getS3OutputParquetKey() {
		return s3OutputParquetKey;
	}

	public void setS3OutputParquetKey(String s3OutputParquetKey) {
		this.s3OutputParquetKey = s3OutputParquetKey;
	}

	public Map<String, String> fullVersionInformation() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("version", getVersion());
		map.put("buildDate", getVersionDate());
		map.put("gitCommitId", getGitCommitId());
		map.put("databaseUri", String.valueOf(connectionProperties().stream().map(ConnectionAttribute::getDatabaseUrl).toList()));
		map.put("number_of_nods", String.valueOf(connectionProperties().size()));
		map.put("volatile", String.valueOf(!getKubernetesExecutorVolatile().isEmpty()));
		map.put("number_of_volatile_executors", String.valueOf(getKubernetesExecutorNumber()));
		map.put("S3_Input", String.valueOf(!getS3InputApiUri().isEmpty()));
		map.put("S3_Output", String.valueOf(!getS3OutputApiUri().isEmpty()));
		map.put("keycloak server", keycloakServer);
		map.put("keycloak realm", keycloakRealm);
		map.put("keycloak ressource", keycloakResource);
		return map;
	}

	public Map<String, String> lightVersionInformation() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("version", getVersion());
		map.put("buildDate", getVersionDate());
		map.put("gitCommitId", getGitCommitId());
		return map;
	}
	
	

	/**
	 * Unserialize the connection data found in properties
	 * 
	 * @return
	 * @throws ArcException 
	 */
	public List<ConnectionAttribute> connectionProperties() {
		
		if (this.connectionProperties != null) {
			return this.connectionProperties;
		}
		
		connectionProperties = new ArrayList<>();
		
		String[] databaseUrls = ConnectionAttribute.unserialize(this.databaseUrl);
		String[] databaseUsernames = ConnectionAttribute.unserialize(this.databaseUsername);
		String[] databasePasswords = ConnectionAttribute.unserialize(this.databasePassword);
		String[] databaseDriverClassNames = ConnectionAttribute.unserialize(this.databaseDriverClassName);
		
		// driver may only be declared once for all databases
		if (databaseDriverClassNames.length==1 && databaseUrls.length>1)
		{
			String driverClassName = databaseDriverClassNames[0];
			databaseDriverClassNames = new String[databaseUrls.length];
			Arrays.fill(databaseDriverClassNames, driverClassName);
		}

		// fill the connectionProperties
		for (int tokenIndex = 0; tokenIndex < databaseUrls.length; tokenIndex++) {
			
			ConnectionAttribute connectionAttribute = new ConnectionAttribute(databaseUrls[tokenIndex], databaseUsernames[tokenIndex],
					databasePasswords[tokenIndex], databaseDriverClassNames[tokenIndex]);
			
			try {
				connectionAttribute.setHost(remapHostAddress.apply(connectionAttribute.getHost()));
			} catch (ArcException e) {
				this.connectionProperties = null;
				return this.connectionProperties;
			}
			
			connectionProperties
					.add(connectionAttribute);
		}

		// if executors are declared on pool, set kubernetesExecutorNumber to 0
		// cannot have at the same time executors declared on pool and executors declared by kubernetes
		if (connectionProperties.size()>1 && this.getKubernetesExecutorNumber()>0)
		{
			this.setKubernetesExecutorNumber(0);
		}
		
		
		// if kubernetes active, add the number of executors nods to connection pool
		// this can happen if and only if one coordinator had been declared in pool
		if (this.kubernetesExecutorNumber > 0)
		{
			for (int i=0; i<this.getKubernetesExecutorNumber(); i++)
			{
				connectionProperties
				.add(new ConnectionAttribute(
						KubernetesServiceLayer.getUri(this.kubernetesExecutorLabel, i, this.kubernetesExecutorDatabase, this.kubernetesExecutorPort) //
						, this.kubernetesExecutorUser
						, this.databasePassword //
						, this.databaseDriverClassName //
						));					
			}
		}
		return this.connectionProperties;

	}
	
	/**
	 * Retrieve connection attributes for a given connection
	 * @param c
	 * @return
	 * @throws ArcException
	 */
	public ConnectionAttribute retrieveConnectionAttribute(Connection c) throws ArcException
	{
		List<ConnectionAttribute> infos = this.connectionProperties();
		try {
			String databaseUrl= c.getMetaData().getURL();
			String userName = c.getMetaData().getUserName();
			return infos.stream()
					.filter(t-> t.getDatabaseUrl().equals(databaseUrl) 
							&& t.getDatabaseUsername().equals(userName))
					.findFirst().get();

		} catch (SQLException | NoSuchElementException e) {
			throw new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_FAILED);
		}	
	}

	public void setConnectionProperties(List<ConnectionAttribute> connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	public int numberOfNods() {
		return this.connectionProperties().size();
	}

	public void setRemapHostAddress(ThrowingFunction<String, String> remapHostAddress) {
		this.remapHostAddress = remapHostAddress;
	}

	public String getKeycloakRealm() {
		return keycloakRealm;
	}

	public void setKeycloakRealm(String keycloakRealm) {
		this.keycloakRealm = keycloakRealm;
	}

	public String getKeycloakServer() {
		return keycloakServer;
	}

	public void setKeycloakServer(String keycloakServer) {
		this.keycloakServer = keycloakServer;
	}

	public String getKeycloakResource() {
		return keycloakResource;
	}

	public void setKeycloakResource(String keycloakResource) {
		this.keycloakResource = keycloakResource;
	}

	public String getKeycloakCredential() {
		return keycloakCredential;
	}

	public void setKeycloakCredential(String keycloakCredential) {
		this.keycloakCredential = keycloakCredential;
	}

	public Integer getFilesRetentionDays() {
		return filesRetentionDays;
	}

	public void setFilesRetentionDays(Integer filesRetentionDays) {
		this.filesRetentionDays = filesRetentionDays;
	}

}
