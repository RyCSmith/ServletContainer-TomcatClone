package edu.upenn.cis.cis455.webserver;

import java.util.HashMap;

import javax.servlet.http.HttpServlet;

public class ServerConfiguration {
	HashMap<String, HttpServlet> servlets;
	HashMap<String, String> urlRoutingMap;
	HashMap<String, HttpSessionClone> sessionsReference;
	int maxNumberOfRequests;
	int maxNumberOfHandlers;
	String rootDirectory;
	ServletContextClone servletContext;
	
	ServerConfiguration(HashMap<String, HttpServlet> servlets, 
			HashMap<String, String> urlRoutingMap, HashMap<String, HttpSessionClone> sessionsReference, 
			int maxNumberOfRequests, int maxNumberOfHandlers, String rootDirectory, ServletContextClone servletContext) {
		this.servlets = servlets;
		this.urlRoutingMap = urlRoutingMap;
		this.sessionsReference = sessionsReference;
		this.maxNumberOfHandlers = maxNumberOfHandlers;
		this.maxNumberOfRequests = maxNumberOfRequests;
		this.rootDirectory = rootDirectory;
		this.servletContext = servletContext;
	}
	
}
