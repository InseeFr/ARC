package fr.insee.arc.core.service.kubernetes.bo;

import org.springframework.http.HttpMethod;

public class RestQuery {

	private String uri;

	private HttpMethod httpMethod;
	
	public String getUri() {
		return uri;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public RestQuery setUri(String uri) {
		this.uri = uri;
		return this;
	}

	public RestQuery setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
		return this;
	}
	
	
	
}
