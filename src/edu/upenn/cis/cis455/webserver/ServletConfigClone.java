package edu.upenn.cis.cis455.webserver;

import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ServletConfigClone implements ServletConfig {
	String name;
	String servletClass;
	HashMap<String, String> initParams;
	ServletContextClone context;
	
	ServletConfigClone() {
		initParams = new HashMap<String, String>();
		name = null;
		servletClass = null;
		context = null;
	}
	
	ServletConfigClone(String name, String servletClass, 
			HashMap<String, String> initParams, ServletContextClone context) {
		this.name = name;
		this.servletClass = servletClass;
		this.initParams = initParams;
		this.context = context;
	}
	
	@Override
	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	void addInitParam(String name, String value) {
		initParams.put(name, value);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return java.util.Collections.enumeration(initParams.keySet());
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}
	
	void setServletContext(ServletContextClone context) {
		this.context = context;
	}

	@Override
	public String getServletName() {
		return name;
	}
	
	void setServletName(String name) {
		this.name = name;
	}

}
