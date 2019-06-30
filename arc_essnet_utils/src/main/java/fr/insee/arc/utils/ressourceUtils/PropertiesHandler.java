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
//public class PropertiesHandler implements ApplicationListener<ContextStartedEvent> {
	public class PropertiesHandler {

    private String databasePoolName;
    private String databaseArcUrl;
    private String databaseArcUsername;
    private String databaseArcPassword;
    private String databaseArcDriverClassName;
    private String databaseArcSchema;
    private String annuaireArcUri;
    private String annuaireArcIdent;
    private String annuaireArcPassword;
    private String logChemin;
    private String logNiveau;
    private String logConfiguration;
    private String batchParametreRepertoire;
    private String batchExecutionEnvironment;
    private String batchArcEnvironment;
    private int threadsChargement;
    private int threadsNormage;
    private int threadsControle;
    private int threadsFiltrage;
    private int threadsMapping;
    private int threadsRegle;
    private int threadNombre;
    private String version;
    private String schemaReference;
    private Boolean isProd;
    private String application;
    private String tn;
    private String repertoireRoot;
    private String repertoireReception;
    private String repertoireChargement;
    private String repertoireStockage;


    public void initializeLog() {
	URL log4jprops = this.getClass().getClassLoader().getResource(logConfiguration);
	// si la ressource est une ressource interne au projet : du type
	// "fr/insee/config/log4j.xml"
	DOMConfigurator.configure(log4jprops);

    }


    public static PropertiesHandler getInstance() {
	GenericXmlApplicationContext ctx = new GenericXmlApplicationContext("applicationContext.xml");

	PropertiesHandler propertitiesHandler = (PropertiesHandler) ctx.getBean("properties");
	ctx.close();
	return propertitiesHandler;
    }

//    @Override
//    public void onApplicationEvent(ContextStartedEvent event) {
//	initializeLog();
//	
//    }


	public String getDatabasePoolName() {
		return databasePoolName;
	}


	public void setDatabasePoolName(String databasePoolName) {
		this.databasePoolName = databasePoolName;
	}


	public String getDatabaseArcUrl() {
		return databaseArcUrl;
	}


	public void setDatabaseArcUrl(String databaseArcUrl) {
		this.databaseArcUrl = databaseArcUrl;
	}


	public String getDatabaseArcUsername() {
		return databaseArcUsername;
	}


	public void setDatabaseArcUsername(String databaseArcUsername) {
		this.databaseArcUsername = databaseArcUsername;
	}


	public String getDatabaseArcPassword() {
		return databaseArcPassword;
	}


	public void setDatabaseArcPassword(String databaseArcPassword) {
		this.databaseArcPassword = databaseArcPassword;
	}


	public String getDatabaseArcDriverClassName() {
		return databaseArcDriverClassName;
	}


	public void setDatabaseArcDriverClassName(String databaseArcDriverClassName) {
		this.databaseArcDriverClassName = databaseArcDriverClassName;
	}


	public String getDatabaseArcSchema() {
		return databaseArcSchema;
	}


	public void setDatabaseArcSchema(String databaseArcSchema) {
		this.databaseArcSchema = databaseArcSchema;
	}


	public String getAnnuaireArcUri() {
		return annuaireArcUri;
	}


	public void setAnnuaireArcUri(String annuaireArcUri) {
		this.annuaireArcUri = annuaireArcUri;
	}


	public String getAnnuaireArcIdent() {
		return annuaireArcIdent;
	}


	public void setAnnuaireArcIdent(String annuaireArcIdent) {
		this.annuaireArcIdent = annuaireArcIdent;
	}


	public String getAnnuaireArcPassword() {
		return annuaireArcPassword;
	}


	public void setAnnuaireArcPassword(String annuaireArcPassword) {
		this.annuaireArcPassword = annuaireArcPassword;
	}


	public String getLogChemin() {
		return logChemin;
	}


	public void setLogChemin(String logChemin) {
		this.logChemin = logChemin;
	}


	public String getLogNiveau() {
		return logNiveau;
	}


	public void setLogNiveau(String logNiveau) {
		this.logNiveau = logNiveau;
	}


	public String getLogConfiguration() {
		return logConfiguration;
	}


	public void setLogConfiguration(String logConfiguration) {
		this.logConfiguration = logConfiguration;
		// TODO use spring Log4jConfigListener ?
		initializeLog();
	}


	public String getBatchParametreRepertoire() {
		return batchParametreRepertoire;
	}


	public void setBatchParametreRepertoire(String batchParametreRepertoire) {
		this.batchParametreRepertoire = batchParametreRepertoire;
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


	public int getThreadsChargement() {
		return threadsChargement;
	}


	public void setThreadsChargement(int threadsChargement) {
		this.threadsChargement = threadsChargement;
	}


	public int getThreadsNormage() {
		return threadsNormage;
	}


	public void setThreadsNormage(int threadsNormage) {
		this.threadsNormage = threadsNormage;
	}


	public int getThreadsControle() {
		return threadsControle;
	}


	public void setThreadsControle(int threadsControle) {
		this.threadsControle = threadsControle;
	}


	public int getThreadsFiltrage() {
		return threadsFiltrage;
	}


	public void setThreadsFiltrage(int threadsFiltrage) {
		this.threadsFiltrage = threadsFiltrage;
	}


	public int getThreadsMapping() {
		return threadsMapping;
	}


	public void setThreadsMapping(int threadsMapping) {
		this.threadsMapping = threadsMapping;
	}


	public int getThreadsRegle() {
		return threadsRegle;
	}


	public void setThreadsRegle(int threadsRegle) {
		this.threadsRegle = threadsRegle;
	}


	public int getThreadNombre() {
		return threadNombre;
	}


	public void setThreadNombre(int threadNombre) {
		this.threadNombre = threadNombre;
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


	public Boolean getIsProd() {
		return isProd;
	}


	public void setIsProd(Boolean isProd) {
		this.isProd = isProd;
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


	public String getRepertoireRoot() {
		return repertoireRoot;
	}


	public void setRepertoireRoot(String repertoireRoot) {
		this.repertoireRoot = repertoireRoot;
	}


	public String getRepertoireReception() {
		return repertoireReception;
	}


	public void setRepertoireReception(String repertoireReception) {
		this.repertoireReception = repertoireReception;
	}


	public String getRepertoireChargement() {
		return repertoireChargement;
	}


	public void setRepertoireChargement(String repertoireChargement) {
		this.repertoireChargement = repertoireChargement;
	}


	public String getRepertoireStockage() {
		return repertoireStockage;
	}


	public void setRepertoireStockage(String repertoireStockage) {
		this.repertoireStockage = repertoireStockage;
	}

    
    
    
}
