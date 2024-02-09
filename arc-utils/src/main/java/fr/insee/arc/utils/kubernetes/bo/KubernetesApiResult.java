package fr.insee.arc.utils.kubernetes.bo;

public class KubernetesApiResult {

	public KubernetesApiResult(int responseCode, String response) {
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

	@Override
	public String toString()
	{
		return this.responseCode+"\n";
	}
	
	
	
}
