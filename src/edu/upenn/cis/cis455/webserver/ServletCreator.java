package edu.upenn.cis.cis455.webserver;

import java.io.File;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ServletCreator {
	static class Handler extends DefaultHandler {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				state = 1;
			} else if (qName.compareTo("servlet-class") == 0) {
				state = 2;
			} else if (qName.compareTo("context-param") == 0) {
				state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				state = 4;
			} else if (qName.compareTo("param-name") == 0) {
				state = (state == 3) ? 10 : 20;
			} else if (qName.compareTo("param-value") == 0) {
				state = (state == 10) ? 11 : 21;
			} else if (qName.compareTo("url-pattern") == 0) {
				state = 5;
			} else if (qName.compareTo("display-name") == 0) {
				state = 6;
			}
		}
		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (state == 1) {
				servletName = value;
				state = 0;
			} else if (state == 2) {
				servletsMap.put(servletName, value);
				state = 0;
			} else if (state == 10 || state == 20) {
				paramName = value;
			} else if (state == 11) {
				if (paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				contextParamsMap.put(paramName, value);
				paramName = null;
				state = 0;
			} else if (state == 21) {
				if (paramName == null) {
					System.err.println("Servlet parameter value '" + value + "' without name");
					System.exit(-1);
				}
				HashMap<String,String> p = initParamsMap.get(servletName);
				if (p == null) {
					p = new HashMap<String,String>();
					initParamsMap.put(servletName, p);
				}
				p.put(paramName, value);
				paramName = null;
				state = 0;
			} else if (state == 5) {
				urlRoutingMap.put(value, servletName);
				state = 0;
			} else if (state == 6) {
				displayName = value;
				state = 0;
			}
		}
		private int state = 0;
		private String servletName;
		private String paramName;
		String displayName;
		HashMap<String,String> servletsMap = new HashMap<String,String>();
		HashMap<String,String> contextParamsMap = new HashMap<String,String>();
		HashMap<String,HashMap<String,String>> initParamsMap = new HashMap<String,HashMap<String,String>>();
		HashMap<String, String> urlRoutingMap = new HashMap<String, String>();
	}
		
	static Handler parseWebDotXML(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	}
	
	static ServletContextClone createContext(Handler handler, String webRoot, String port) throws UnknownHostException {
		HashMap<String, String> initParams = handler.contextParamsMap;
		String displayName = handler.displayName;
		HashMap<String, Object> emptyAttributesMap = new HashMap<String, Object>();
		ServletContextClone context = new ServletContextClone(displayName, webRoot, initParams, emptyAttributesMap);
		context.setAttribute("ip", Inet4Address.getLocalHost().getHostAddress());
		context.setAttribute("port", port);
		return context;
	}
	
	static HashMap<String, HttpServlet> createServlets(Handler handler, ServletContextClone context) throws Exception {
		HashMap<String, HttpServlet> servletsMap = new HashMap<String, HttpServlet>();
		for (String servletName : handler.servletsMap.keySet()) {
			String servletClassName = handler.servletsMap.get(servletName);
			HashMap<String, String> initParams = handler.initParamsMap.get(servletName);
			ServletConfigClone config = new ServletConfigClone(servletName, servletClassName, initParams, context);
			Class servletClass = Class.forName(servletClassName);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			servlet.init(config);
			servletsMap.put(servletName, servlet);
		}
		return servletsMap;
	}
}
