package fr.insee.arc.utils.kubernetes.bo;

import org.springframework.http.HttpStatus;

public class KubernetesApiResult {

	public KubernetesApiResult(String sourceInformation, int responseCode, String response) {
		super();
		this.sourceInformation = sourceInformation;
		this.responseCode = responseCode;
		this.response = response;
	}
	
	
	private int responseCode;
	private String response;
	private String sourceInformation;
	
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

	@Override
	public String toString()
	{
		return this.sourceInformation+"\n"+HttpStatus.valueOf(this.responseCode).name()+"\n\n";
	}
	
	
}
