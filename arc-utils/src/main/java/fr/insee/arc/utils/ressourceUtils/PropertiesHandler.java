package fr.insee.arc.utils.ressourceUtils;


import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component("properties")
@Getter
@Setter
public class PropertiesHandler {
	
    /* Database */
    private String databasePoolName;
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
    private String logPath;
    private String logLevel;
    private String logConfiguration;
    /* Batch */
    private String batchParametersDirectory;
    private String batchExecutionEnvironment;
    private String batchArcEnvironment;
    /* Miscellaneous */
    private String version;
    private String schemaReference;
    private String application;
    private String tn;
    /* Directories */
    private String registrationDirectory;
    private String loadingDirectory;
    private String storageDirectory;
    /* lang */
    private String lang;

    public void initializeLog() {
        URL log4jprops = this.getClass().getClassLoader().getResource(logConfiguration);

        // Using here an XML configuration
        DOMConfigurator.configure(log4jprops);
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


    public String getLogPath() {
        return logPath;
    }


    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }


    public String getLogLevel() {
        return logLevel;
    }


    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
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


    public String getSchemaReference() {
        return schemaReference;
    }


    public void setSchemaReference(String schemaReference) {
        this.schemaReference = schemaReference;
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


	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

    
    
}
