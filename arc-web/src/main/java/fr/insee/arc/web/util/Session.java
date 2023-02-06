package fr.insee.arc.web.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
		httpSession.setAttribute(attributeName, attributeValue);
	}

	public void remove(String attributeName) {
		httpSession.removeAttribute(attributeName);		
	}

}