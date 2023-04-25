package fr.insee.arc.web.util;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Session {


	@Autowired
	private HttpSession httpSession;

	public Object get(String attributeName) {
		return httpSession.getAttribute(attributeName);
	}

	public void put(String attributeName, Object attributeValue) {
		if (httpSession!=null)
		{
			httpSession.setAttribute(attributeName, attributeValue);
		}
	}

	public void remove(String attributeName) {
		httpSession.removeAttribute(attributeName);		
	}

	public HttpSession getHttpSession() {
		return httpSession;
	}

	public void setHttpSession(HttpSession httpSession) {
		this.httpSession = httpSession;
	}

	
}