package edu.upenn.cis.cis455.webserver;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class ServletContextClone implements ServletContext {
	String appDisplayName;
	String webRoot = null;
	HashMap<String, String> initParams;
	HashMap<String, Object> attributes;
	
	ServletContextClone() {
		appDisplayName = null;
		webRoot = null;
		initParams = new HashMap<String, String>();
		attributes = new HashMap<String, Object>();
	}
	
	ServletContextClone(String appDisplayName, String webRoot, 
			HashMap<String, String> initParams, HashMap<String, Object>attributes) {
		this.appDisplayName = appDisplayName;
		this.webRoot = webRoot;
		this.initParams = initParams;
		this.attributes = attributes;
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
	public ServletContext getContext(String uri) {
		return this;
	}

	@Override
	public String getInitParameter(String name) {
		return initParams.get(name);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return java.util.Collections.enumeration(initParams.keySet());

	}

	@Override
	public int getMajorVersion() {
		return 2;
	}


	@Override
	public int getMinorVersion() {
		return 4;
	}

	@Override
	public String getRealPath(String path) {
		return webRoot + path;
	}

	@Override
	public String getServerInfo() {
		return "Ryan CIS555 Tomcat Clone/1.0";
	}

	@Override
	public String getServletContextName() {
		return appDisplayName;
	}
	
	void setServletContextName(String name) {
		appDisplayName = name;
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
	
//NOT IMPLEMENTED
	@Override
	public String getMimeType(String arg0) {
		return null;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		return null;
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		return null;
	}

	@Override
	public Set getResourcePaths(String arg0) {
		return null;
	}

	@Override
	public void log(String arg0) {}

	@Override
	public void log(String arg0, Throwable arg1) {}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

//DEPRECATED
	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		return null;
	}
	
	@Override
	public Enumeration getServletNames() {
		return null;
	}

	@Override
	public Enumeration getServlets() {
		return null;
	}
	
	@Override
	public void log(Exception arg0, String arg1) {}
}
