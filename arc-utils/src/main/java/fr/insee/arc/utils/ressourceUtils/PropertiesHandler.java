package fr.insee.arc.utils.ressourceUtils;


import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.ManipString;

@Component("properties")
public class PropertiesHandler {
	
    /* Database */
    private String databasePoolName;
    private String databaseRestrictedUsername;
    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;
    private String databaseDriverClassName;
    private String databaseSchema;
    /* Authentication directory */
    private String ldapDirectoryUri;
    private String ldapDirectoryIdent;
    private String ldapDirectoryPassword;
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
    private String tn;
    /* Directories */
    private String registrationDirectory;
    private String loadingDirectory;
    private String storageDirectory;

    private String authorizedRoles;
    private String disableDebugGui;

    private String gitCommitId;
    
    private static PropertiesHandler instanceOfPropertiesHandler;
    

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
		if (instanceOfPropertiesHandler==null)
		{
	    	try {
	    		instanceOfPropertiesHandler= (PropertiesHandler) SpringApplicationContext.getBean("properties");
	    	} catch( NullPointerException e ) {
	    		ArcException ex= new ArcException(ArcExceptionMessage.SPRING_BEAN_PROPERTIES_NOTFOUND);
	    		ex.logMessageException();
	    		// create a blank instance singleton
	    		instanceOfPropertiesHandler=new PropertiesHandler();
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
    }


    public String getDatabaseUsername() {
        return databaseUsername;
    }


    public void setDatabaseUsername(String databaseUsername) {
        this.databaseUsername = databaseUsername;
    }


    public String getDatabasePassword() {
        return databasePassword;
    }


    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }


    public String getDatabaseDriverClassName() {
        return databaseDriverClassName;
    }


    public void setDatabaseDriverClassName(String databaseDriverClassName) {
        this.databaseDriverClassName = databaseDriverClassName;
    }


    public String getDatabaseSchema() {
        return databaseSchema;
    }


    public void setDatabaseSchema(String databaseSchema) {
        this.databaseSchema = databaseSchema;
    }

    
    public boolean isLdapActive() {
    	return !ldapDirectoryUri.isEmpty();
    }
    
    public String getLdapApplicatioName() {
    	if (ldapDirectoryIdent.isEmpty()){
    		return "";
    	}
    	return ldapDirectoryIdent.substring("appli_".length());
    }

    public String getLdapDirectoryUri() {
        return ldapDirectoryUri;
    }


    public void setLdapDirectoryUri(String ldapDirectoryUri) {
        this.ldapDirectoryUri = ldapDirectoryUri;
    }


    public String getLdapDirectoryIdent() {
        return ldapDirectoryIdent;
    }


    public void setLdapDirectoryIdent(String ldapDirectoryIdent) {
        this.ldapDirectoryIdent = ldapDirectoryIdent;
    }


    public String getLdapDirectoryPassword() {
        return ldapDirectoryPassword;
    }


    public void setLdapDirectoryPassword(String ldapDirectoryPassword) {
        this.ldapDirectoryPassword = ldapDirectoryPassword;
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


    public String getTn() {
        return tn;
    }


    public void setTn(String tn) {
        this.tn = tn;
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
	
	public Map<String,String> fullVersionInformation()
	{
		Map<String,String> map = new LinkedHashMap<>();
		map.put("version", getVersion());
		map.put("buildDate", getVersionDate());
		map.put("gitCommitId", getGitCommitId());
		map.put("databaseUrl", getDatabaseUrl());
		map.put("databaseUserName", getDatabaseUsername());
		return map;
	}
    
}
