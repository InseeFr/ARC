package fr.insee.arc.utils.ressourceUtils;


import java.io.File;
import java.net.URL;
import java.util.Map;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.stereotype.Component;

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
    private String logConfiguration;
    /* Batch */
    private String batchParametersDirectory;
    private String batchExecutionEnvironment;
    private String batchArcEnvironment;
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

    public void initializeLog() {
   	
    	LoggerContext context;
    	
        // Using here an XML configuration
        URL log4jprops = this.getClass().getClassLoader().getResource(logConfiguration);
        if (log4jprops != null) {
        	context=Configurator.initialize(null, log4jprops.toString());
        } else {
        	File file = new File(logConfiguration);
        	context=Configurator.initialize(null, file.getAbsolutePath());
        } 
        
        // replace ConsoleAppender with FileAppender if a logDirectory (fr.insee.arc.log.directory) is set
        // TODO remove xml configuration ??
        if (logDirectory!=null && !logDirectory.trim().equals(""))
        {
	        Configuration config = context.getConfiguration();
	        @SuppressWarnings("deprecation")
	        
	        // create the rolling file appender
	        // TODO remove deprecated method
	        // should be easy as the deprecated method source code uses the new method despite the new method doesn't implement default values....
			Appender appender = RollingFileAppender
	        		.createAppender(
	        				this.logDirectory + File.separator+ "arc.log"
	        				, this.logDirectory + File.separator+ "arc-%d{MM-dd-yyyy}.log"
	        				, null
	        				, "File"
	        				, null
	        				, null
	        				, null
	        				, TimeBasedTriggeringPolicy.newBuilder().withInterval(1).withModulate(true).build()
	        				, null
	        				, PatternLayout.newBuilder().withPattern("%d %p %c [%t] %m%n").build()
	        				,null
	        				,null
	        				,null
	        				,null
	        				,config
	        				);
	      	appender.start();
	      	config.addAppender(appender);
	
	      	// replace every console appender by the the rollingfileappender
	      	Map<String, LoggerConfig> loggerConfig = config.getLoggers();
	      	for (LoggerConfig l:loggerConfig.values())
	      	{
	      			for (Appender z:l.getAppenders().values())
	      			{
	      				if (z.getClass().getSimpleName().equals(ConsoleAppender.class.getSimpleName()))
	      				{
	      	      			l.removeAppender(z.getName());
	      	      			l.addAppender(appender, null, null);
	      				}
	      			}
	      	}
	      	
	      	// apply
	      	context.updateLoggers();
        }
    }


    public static PropertiesHandler getInstance() {
    	return (PropertiesHandler) SpringApplicationContext.getBean("properties");
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


    public String getBatchArcEnvironment() {
        return batchArcEnvironment;
    }


    public void setBatchArcEnvironment(String batchArcEnvironment) {
        this.batchArcEnvironment = batchArcEnvironment;
    }


     public String getVersion() {
        return version;
    }


    public void setVersion(String version) {
        this.version = version;
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


	public String getLogDirectory() {
		return logDirectory;
	}


	public void setLogDirectory(String logDirectory) {
		this.logDirectory = logDirectory;
	}
	
	
    
}
