package fr.insee.arc.web.gui.maintenanceoperation.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.gui.all.util.VObject;

@Component
public class MaintenanceOperationsModel implements ArcModel {
	
	private String url;
	private String httpType;
	private String json;
	private String httpOutput;
	private String token;
	private int numberOfExecutorDatabase;
	
    private VObject viewOperations;
    private VObject viewKubernetes;


    public MaintenanceOperationsModel() {
		this.viewOperations = new ViewOperations();
		this.viewKubernetes = new ViewKubernetes();
	}

	public VObject getViewOperations() {
		return viewOperations;
	}

	public void setViewOperations(VObject viewOperations) {
		this.viewOperations = viewOperations;
	}

	public VObject getViewKubernetes() {
		return viewKubernetes;
	}

	public void setViewKubernetes(VObject viewKubernetes) {
		this.viewKubernetes = viewKubernetes;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHttpType() {
		return httpType;
	}

	public void setHttpType(String httpType) {
		this.httpType = httpType;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getHttpOutput() {
		return httpOutput;
	}

	public void setHttpOutput(String httpOutput) {
		this.httpOutput = httpOutput;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getNumberOfExecutorDatabase() {
		return numberOfExecutorDatabase;
	}

	public void setNumberOfExecutorDatabase(int numberOfExecutorDatabase) {
		this.numberOfExecutorDatabase = numberOfExecutorDatabase;
	}

	
	
	
}
