package fr.insee.arc.ws.services.rest.execute.pojo;

import java.util.List;

/**
 * Parameters for ARC steps execution
 * @author MS
 *
 */
public class ExecuteParameterPojo {

	// nom du fichier
	public String fileName;
	
	// contenu du fichier
	public String fileContent;
	
	// environnement d'execution
	public String sandbox;
	
	// n° de la phase à atteindre
	public String targetPhase;

	// Type d'invoquation : SERVICE ou ENGINE
	public String serviceType;
	
	// norme (~version)
	public String norme;
	
	// validite
	public String validite;
	
	// periodicite
	public String periodicite;
	
	// liste des requete à exécuter
	public List<ExecuteQueryPojo> queries;


	
	
	public String getSandbox() {
		return sandbox;
	}
	public void setSandbox(String sandbox) {
		this.sandbox = sandbox;
	}
	public String getTargetPhase() {
		return targetPhase;
	}
	public void setTargetPhase(String targetPhase) {
		this.targetPhase = targetPhase;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getVersion() {
		return norme;
	}
	public void setVersion(String version) {
		this.norme = version;
	}
	public String getValidite() {
		return validite;
	}
	public void setValidite(String validite) {
		this.validite = validite;
	}
	public String getPeriodicite() {
		return periodicite;
	}
	public void setPeriodicite(String periodicite) {
		this.periodicite = periodicite;
	}
	public List<ExecuteQueryPojo> getQueries() {
		return queries;
	}
	public void setQueries(List<ExecuteQueryPojo> queries) {
		this.queries = queries;
	}
	public String getFileContent() {
		return fileContent;
	}
	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}
	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	
	
	
}
