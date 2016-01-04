package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;


public class HttpServletRequestClone implements HttpServletRequest {
	static final Logger logger = Logger.getLogger(HttpServletRequestClone.class);
	static final String BASIC_AUTH = "BASIC";
	static final String DEFAULT_CHAR_ENC = "ISO-8859-1";
	private HttpSessionClone session = null;
	private HashMap<String, String> parameterMap;
	private HashMap<String, String> headersMap;
	private HashMap<String, Object> attributes;
	private HashMap<String, String> cookiesMap;
	private HashMap<String, HttpSessionClone> sessionsReference;
	private Random rand = new Random();
	private ServletContextClone context;
	private Socket clientSocket;
	private HttpServletResponseClone response;
	
	HttpServletRequestClone() {
		parameterMap = new HashMap<String, String>();
		attributes = new HashMap<String, Object>();
		attributes.put("charEncoding", DEFAULT_CHAR_ENC);
	}
	
	HttpServletRequestClone(HashMap<String, HttpSessionClone> sessionsReference, ServletContextClone context, 
			HashMap<String, String> cookiesMap, HashMap<String, String> headersMap, Socket clientSocket) {
		this();
		this.sessionsReference = sessionsReference;
		this.context = context;
		this.cookiesMap = cookiesMap;
		this.headersMap = headersMap;
		this.clientSocket = clientSocket;
		if (cookiesMap.get("JSESSIONID") != null) {
			session = sessionsReference.get(cookiesMap.get("JSESSIONID"));
			if (session != null)
				session.isNew = false;
		}
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
	public String getCharacterEncoding() {
		return (String) attributes.get("charEncoding");
	}

	@Override
	public int getContentLength() {
		try {
			int length = Integer.parseInt(headersMap.get("Content-Length"));
			return length;
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public String getContentType() {
		return headersMap.get("Content-Type");
	}

	@Override
	public String getLocalAddr() {
		String address = headersMap.get("Host");
		if (address != null)
			if (address.equals("localhost"))
				try {
					return Inet4Address.getLocalHost().getHostAddress();
				} catch(UnknownHostException e) {
					logger.error(e);
				}
		return address;
	}

	@Override
	public String getLocalName() {
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			logger.error(e);
		}
		return addr.getHostName();
	}

	@Override
	public int getLocalPort() {
		return Integer.parseInt(headersMap.get("Port"));
	}
	
	public void setLocale(Locale locale) {
		attributes.put("locale", locale);
	}
	
	@Override
	public Locale getLocale() {
		return (Locale) attributes.get("locale");
	}
	
	void setParameter(String name, String value) {
		parameterMap.put(name, value);
	}
	
	@Override
	public String getParameter(String name) {
		return parameterMap.get(name);
	}

	@Override
	public Map getParameterMap() {
		return parameterMap;
	}

	@Override
	public Enumeration getParameterNames() {
		return java.util.Collections.enumeration(parameterMap.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		String value = parameterMap.get(name);
		String[] vals;
		if (!(value == null)){
			if (value.contains(",")){
				vals = value.split(",");
				for(int i = 0; i < vals.length; i++) {
					vals[i] = vals[i].trim();
				}
			}
			else {
				vals = new String[1];
				vals[0] = value;
			}
		}
		else
			vals = new String[0];
		return vals;
	}

	@Override
	public String getProtocol() {
		return headersMap.get("Protocol");
	}

	@Override
	public BufferedReader getReader() throws IOException {
		String body = (String) attributes.get("body");
		if (body != null)
			return new BufferedReader(new StringReader(body));
		return null;
	}

	@Override
	public String getRemoteAddr() {
		InetAddress clientIP = clientSocket.getInetAddress();
		return clientIP.getHostAddress();
	}

	@Override
	public String getRemoteHost() {
		InetAddress clientIP = clientSocket.getInetAddress();
		return clientIP.getCanonicalHostName();
	}

	@Override
	public int getRemotePort() {
		return clientSocket.getPort();
	}

	@Override
	public String getScheme() {
		return "http";
	}

	@Override
	public String getServerName() {
		return headersMap.get("Host");
	}

	@Override
	public int getServerPort() {
		return Integer.parseInt(headersMap.get("Port"));
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	@Override
	public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
		try {
			Charset.isSupported(enc);
			attributes.put("charEncoding", enc);
		} catch (Exception e) {
			throw new UnsupportedEncodingException("The provided encoding: " + enc + " is not supported.");
		}
	}

	@Override
	public String getAuthType() {
		return BASIC_AUTH;
	}

	@Override
	public String getContextPath() {
		return "";
	}

	@Override
	public Cookie[] getCookies() {
		Cookie[] cookies = new Cookie[cookiesMap.keySet().size()];
		int counter = 0;
		for (String key : cookiesMap.keySet()) {
			cookies[counter] = new Cookie(key, cookiesMap.get(key));
		}
		return cookies;
	}

	@Override
	public long getDateHeader(String header) {
		String stringDate = headersMap.get(header);
		if (stringDate != null) {
			Date requestDate;
			try {
				SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
				requestDate = format.parse(stringDate);
			} catch (ParseException e0) {
				try {
					SimpleDateFormat format = new SimpleDateFormat("EEE, dd-MMM-yy HH:mm:ss zzz");
					requestDate = format.parse(stringDate);
				}catch (ParseException e1) {
					try {
						SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
						requestDate = format.parse(stringDate);
					}catch (ParseException e2) {
						throw new IllegalArgumentException();
					}
				}
			}
			Calendar requestTime = Calendar.getInstance();
			requestTime.setTime(requestDate);
			return requestTime.getTimeInMillis();
		}
		return -1;
	}

	@Override
	public String getHeader(String header) {
		return headersMap.get(header);
	}

	@Override
	public Enumeration getHeaderNames() {
		return java.util.Collections.enumeration(headersMap.keySet());
	}

	@Override
	public Enumeration getHeaders(String header) {
		String valueString = headersMap.get(header);
		ArrayList<String> vals = new ArrayList<String>();
		if (valueString != null){
			if (valueString.contains(",")){
				String[] multiVals = valueString.split(",");
				for(int i = 0; i < multiVals.length; i++) {
					vals.add(multiVals[i].trim());
				}
			}
			else {
				vals.add(valueString);
			}
		}
		return java.util.Collections.enumeration(vals);
	}

	@Override
	public int getIntHeader(String header) {
		String valueString = headersMap.get(header);
		if (valueString != null) {
			try {
				int intValue = Integer.parseInt(valueString);
				return intValue;
			} catch (Exception e) {
				throw new NumberFormatException("The header: " + header + " cannot be converted to an int.");
			}
		}
		return -1;
	}

	@Override
	public String getMethod() {
		return (String) attributes.get("method");
	}

	@Override
	public String getPathInfo() {
		char[] regexMatch = ((String)attributes.get("regexMatch")).toCharArray();
		char[] fullPath = getRequestURI().toCharArray();
		int index = 0;
		while (regexMatch[index] == fullPath[index]) {
			index++;
			if (index >= fullPath.length)
				break;
		}
		if (index < fullPath.length){
			char[] unMatchedPortion = Arrays.copyOfRange(fullPath, index, fullPath.length);
			return new String(unMatchedPortion);
		}
		return "";
	}

	@Override
	public String getQueryString() {
		return (String) attributes.get("queryString");
	}
	
	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public String getRequestURI() {
		return (String) attributes.get("URI");
	}

	@Override
	public StringBuffer getRequestURL() {
		StringBuffer url = new StringBuffer("http://");
		url.append(headersMap.get("Host"));
		url.append(":");
		url.append(headersMap.get("Port"));
		url.append(getRequestURI());
		return url;
	}

	@Override
	public String getRequestedSessionId() {
		return cookiesMap.get("JSESSIONID");
	}

	@Override
	public String getServletPath() {
		String regexMatch = (String)attributes.get("regexMatch");
		regexMatch = regexMatch.replaceAll("\\.\\*", "");
		return regexMatch;
	}

	/**
	 * Returns the current session associated with this request, or if the request 
	 * does not have a session, creates one and adds a cookie to the response.
	 */
	@Override
	public HttpSession getSession() throws IllegalStateException {
		if (response.committed){
			logger.error("A new session cookie cannot be added to the "
					+ "response as it has already been committed.");
			throw new IllegalStateException("A new session cookie cannot be added to the "
					+ "response as it has already been committed.");
		}
		if (session != null){
			checkForInvalidSession(session);
			return session;
		}
		String newID = null;
		//loop and create new random 30 character ids until we find a unique one
		while ((newID == null) || (sessionsReference.get(newID) != null)) {
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < 30; i++) {
				char c = (char) (rand.nextInt(93) + 33);
				s.append(c);
			}
			newID = s.toString();
		}
		HttpSessionClone newSession = new HttpSessionClone(newID, context);
		response.addCookie(new Cookie("JSESSIONID", newID));
		sessionsReference.put(newID, newSession);
		this.session = newSession;
		return newSession;
	}
	
	/**
	 * Checks to see if a session has expired and invalidates it if so. If not,
	 * updates last accessed time.
	 * @param session
	 */
	static void checkForInvalidSession(HttpSessionClone session) {
		long expireTime = session.getLastAccessedTime() + session.getMaxInactiveInterval();
		System.out.println("ExpireTime" + expireTime);
		System.out.println("Current Time" + System.currentTimeMillis());
		System.out.println("Last access" + session.getLastAccessedTime());
		System.out.println("Max int" + session.getMaxInactiveInterval());
		if (expireTime < System.currentTimeMillis())
			session.invalidate();
		else
			session.setLastAccessedTime(System.currentTimeMillis());
	}

	/**
	 * Returns the current HttpSession associated with this request or, if there is no current session and create is true, returns a new session.
	 * If create is false and the request has no valid HttpSession, this method returns null.
	 */
	@Override
	public HttpSession getSession(boolean create) {
		if (session != null){
			checkForInvalidSession(session);
			return session;
		}
		else if (create)
			return getSession();
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	/**
	 * Checks whether the requested session ID is still valid.
	 * @return true if this request has an id for a valid session in the current session context; false otherwise
	 */
	@Override
	public boolean isRequestedSessionIdValid() {
		if (session == null)
			return false;
		if (session.isValid)
			return true;
		return false;
	}

	void addResponseClone(HttpServletResponseClone response) {
		this.response = response;
	}
	
//NOT IMPLEMENTEED
	@Override
	public boolean isUserInRole(String arg0) {
		return false;
	}
	
	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}
	
	@Override
	public Enumeration getLocales() {
		return null;
	}
	
//DEPRECATED
	@Override
	public String getRealPath(String arg0) {
		return null;
	}
	
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}
}
