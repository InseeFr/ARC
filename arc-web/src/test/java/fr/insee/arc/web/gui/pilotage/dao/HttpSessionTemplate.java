package fr.insee.arc.web.gui.pilotage.dao;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

public class HttpSessionTemplate implements HttpSession 
{
	
	Map<String, Object> content;


	public HttpSessionTemplate() {
		super();
		content = new HashMap<>();
	}

	@Override
	public long getCreationTime() {
		return 0;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public long getLastAccessedTime() {
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
	}

	@Override
	public int getMaxInactiveInterval() {
		return 0;
	}

	@Override
	public Object getAttribute(String name) {
		return content.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return null;
	}

	@Override
	public void setAttribute(String name, Object value) {
		content.put(name, value);
	}

	@Override
	public void removeAttribute(String name) {
	}

	@Override
	public void invalidate() {
		
	}

	@Override
	public boolean isNew() {
		return false;
	}
	
}