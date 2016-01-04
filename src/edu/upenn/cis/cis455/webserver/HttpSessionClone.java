package edu.upenn.cis.cis455.webserver;

import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class HttpSessionClone implements HttpSession {
	HashMap<String, Object> attributes;
	long creationTime;
	long lastRequestTime;
	String id;
	int maxInactiveInterval;
	ServletContextClone context;
	boolean isValid;
	boolean isNew;
	
	HttpSessionClone() {
		attributes = new HashMap<String, Object>();
		creationTime = System.currentTimeMillis();
		lastRequestTime = creationTime;
		isValid = true;
		maxInactiveInterval = (24 * 60 * 60000); //(ms) one day default session time
		isNew = true;
	}
	
	HttpSessionClone(String id, ServletContextClone context) {
		this();
		this.id = id;
		this.context = context;
	}
	
	HttpSessionClone(HashMap<String, Object> attributes, String id, 
			int maxInactiveInterval, ServletContextClone context) {
		this.attributes = attributes;
		this.id = id;
		this.maxInactiveInterval = maxInactiveInterval;
		this.context = context;
		isValid = true;
		creationTime = System.currentTimeMillis();
		lastRequestTime = creationTime;
	}
	
	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		return java.util.Collections.enumeration(attributes.keySet());

	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getLastAccessedTime() {
		return lastRequestTime;
	}
	
	public void setLastAccessedTime(long time) {
		lastRequestTime = time;
	}
	
	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public void invalidate() {
		attributes.clear();
		isValid = false;
	}

	@Override
	public boolean isNew() {
//?		
		return false;
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
		
	}

	@Override
	public void setAttribute(String name, Object value) {
		if (value == null) 
			removeAttribute(name);
		else
			attributes.put(name, value);
	}

	@Override
	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;	
	}
	
//DEPRECATED
	@Override
	public void removeValue(String arg0) {}
	
	@Override
	public HttpSessionContext getSessionContext() {
		return null;
	}
	
	@Override
	public Object getValue(String arg0) {
		return null;
	}

	@Override
	public String[] getValueNames() {
		return null;
	}
	
	@Override
	public void putValue(String arg0, Object arg1) {}
}
