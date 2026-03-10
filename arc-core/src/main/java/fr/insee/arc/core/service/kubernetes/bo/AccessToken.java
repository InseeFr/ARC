package fr.insee.arc.core.service.kubernetes.bo;

public class AccessToken {

	public AccessToken(String token) {
		super();
		this.token = token;
	}

	private String token;

	public String getToken() {
		return token;
	}

	
}
