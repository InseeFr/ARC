package fr.insee.arc.core.service.kubernetes.bo;

public class KubernetesServiceResult {

	public KubernetesServiceResult(int responseCode, String response) {
		super();
		this.responseCode = responseCode;
		this.response = response;
	}
	private int responseCode;
	private String response;
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}

	
	
	
	
}
