package edu.upenn.cis.cis455.webserver;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class HttpServletResponseClone implements HttpServletResponse {
	
	class FakeWriter extends PrintWriter {
		StringBuffer messageBody;
		
		FakeWriter(BufferedWriter bufwriter) {
			super(bufwriter);
			messageBody = new StringBuffer();
		}
		
		@Override
		public void println(String outputLine) {
			messageBody.append(outputLine);
		}
		
		@Override
		public void write(String outputLine) {
			messageBody.append(outputLine);
		}
		public void send(StringBuffer bufString) {
			super.write(bufString.toString());
			flush();
		}
	}
	HttpServletRequestClone request;
	static final Logger logger = Logger.getLogger(HttpServletResponseClone.class);
	static final String DEFAULT_CHAR_ENC = "ISO-8859-1";
	static final Locale DEFAULT_LOCALE = Locale.US;
	HashMap<String, Object> headers;
	ArrayList<Cookie> cookies;
	FakeWriter writer;
	boolean committed;
	int statusCode;
	String charEnc;
	
	HttpServletResponseClone(OutputStream socketStream, HttpServletRequestClone request) {
		this.request = request;
		headers = new HashMap<String, Object>();
		headers.put("locale", DEFAULT_LOCALE);
		cookies = new ArrayList<Cookie>();
		writer = new FakeWriter(new BufferedWriter(new OutputStreamWriter(socketStream)));
		committed = false;
		statusCode = -1;
		charEnc = DEFAULT_CHAR_ENC;
	}
	
	@Override
	public void flushBuffer() throws IOException {
		committed = true;
		StringBuffer headerBuf = new StringBuffer();
		headerBuf.append("HTTP/1.1 ");
		headerBuf.append(getStatusString());
		headerBuf.append("Connection: close\r\n");
		for (String key : headers.keySet()) {
			headerBuf.append(getHeaderString(key, headers.get(key)));
			headerBuf.append("\r\n");
		}
		if (!(headers.keySet().contains("Content-Length")))
			headerBuf.append("Content-Length: " + writer.messageBody.length() + "\r\n");
		headerBuf.append(getCookieString());
		headerBuf.append("\r\n");
		headerBuf.append(writer.messageBody);
		writer.send(headerBuf);
		writer.close();
	}
	
	private String getCookieString() {
		StringBuilder builder = new StringBuilder();
		for (Cookie cookie : cookies) {
			builder.append("Set-Cookie: ");
			builder.append(cookie.getName() + "=" + cookie.getValue());
			if (cookie.getMaxAge() >= 0){
				long now = System.currentTimeMillis();
				now += cookie.getMaxAge();
				builder.append("; expires=" + new Date(now));	
			}
			if (cookie.getDomain() != null) {
				builder.append("; domain=" + cookie.getDomain());
			}
			if (cookie.getPath() != null) {
				builder.append("; path=" + cookie.getPath());
			}
			if (cookie.getComment() != null) {
				builder.append("; comment=" + cookie.getComment());
			}
			builder.append("\r\n");
		}
		return builder.toString();
	}
	
	private String getHeaderString(String key, Object value) {
		if (value instanceof String)
			return key + ": " + (String) value;
		if (value instanceof Integer)
			return key + ": " + value.toString();
		if (value instanceof Date)
			return key + ": " + value;
		if (value instanceof Locale)
			return "Content-Language: " + ((Locale) value).getISO3Language();
		return key + ": " + value.toString();
	}
	
	private String getStatusString() {
		switch (statusCode) {
			case(200):
				return "200 OK\r\n";
			case(400):
				return "400 Bad Request\r\n";
			case(403):
				return "403 Forbidden\r\n";
			case(404):
				return "404 Not Found\r\n";
			case(500):
				return "500 Server Error\r\n";
			case(304):
				return "304 Not Modified\r\n";
			case(412):
				return "412 Precondition Failed\r\n";
			case(301):
				return "301 Moved Permanently\r\n";
			case(-1)://when it was never set
				return "";
			default:
				return new Integer(statusCode).toString() + "\r\n";
		}
	}

	@Override
	public int getBufferSize() {
		return writer.messageBody.capacity();
	}

	@Override
	public String getCharacterEncoding() {
		return DEFAULT_CHAR_ENC;
	}

	@Override
	public String getContentType() {
		String type = (String) headers.get("Content-Type");
		if (type == null)
			return "text/html";
		else
			return type;
	}

	@Override
	public Locale getLocale() {
		return (Locale) headers.get("locale");
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return writer;
	}

	@Override
	public boolean isCommitted() {
		return committed;
	}

	/**
	 * Clears any data that exists in the buffer as well as the status code and 
	 * headers. If the response has been committed, this method throws an IllegalStateException.
	 */
	@Override
	public void reset() {
		resetBuffer();
		statusCode = -1;
		headers.clear();
	}

	/**
	 * Clears the content of the underlying buffer in the response without 
	 * clearing headers or status code. If the response has been committed, 
	 * this method throws an IllegalStateException.
	 */
	@Override
	public void resetBuffer() {
		if (committed)
			throw new IllegalStateException("Response Buffer cannot "
					+ "be reset after response has been committed.");
		writer.messageBody = new StringBuffer();
	}

	@Override
	public void setBufferSize(int size) {
		writer.messageBody.ensureCapacity(size);
	}

	@Override
	public void setCharacterEncoding(String enc) {
		charEnc = enc;
	}

	@Override
	public void setContentLength(int size) {
		headers.put("Content-Length", size);
	}

	@Override
	public void setContentType(String type) {
		headers.put("Content-Type", type);
	}

	@Override
	public void setLocale(Locale locale) {
		headers.put("locale", locale);
	}

	@Override
	public void addCookie(Cookie cookie) {
		cookies.add(cookie);
	}

	@Override
	public void addDateHeader(String name, long dateInMillis) {
		Date date = new Date(dateInMillis);
		headers.put(name, date);
	}

	@Override
	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	@Override
	public void addIntHeader(String name, int intValue) {
		headers.put(name, new Integer(intValue));
	}

	@Override
	public boolean containsHeader(String name) {
		return (headers.get(name) != null);
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return url;
	}

	@Override
	public String encodeUrl(String url) {
		return url;
	}

	/**
	 * Sends an error response to the client using the specified status code and clearing the buffer.
	 * If the response has already been committed, this method throws an IllegalStateException. After using 
	 * this method, the response should be considered to be committed and should not be written to.
	 */
	@Override
	public void sendError(int code) throws IOException {
		logger.error("Servlet is attempting to send errorResponse to client with code: " + code);
		if (committed){
			logger.error("IllegalStateException in response.sendError(). Response has already been committed.");
			throw new IllegalStateException("sendError() cannot be completed. "
					+ "The response has already been committed.");
		}
		reset();
		statusCode = code;
		headers.put("Date", new Date());
		setContentType("text/html");
		writer.println(getErrorText(code));
		flushBuffer();
		committed = true;
	}

	/**
	 * Sends an error response to the client using the specified status. The server defaults to creating 
	 * the response to look like an HTML-formatted server error page containing the specified message, 
	 * setting the content type to "text/html", leaving cookies and other headers unmodified. If an error-page 
	 * declaration has been made for the web application corresponding to the status code passed in, it will 
	 * be served back in preference to the suggested msg parameter. If the response has already been committed, 
	 * this method throws an IllegalStateException. After using this method, the response should be considered 
	 * to be committed and should not be written to.
	 */
	@Override
	public void sendError(int code, String message) throws IOException {
		logger.error("Servlet is attempting to send errorResponse to client with code: " + 
				code + " and message: " + message);
		if (committed){
			logger.error("IllegalStateException in response.sendError(). Response has already been committed.");
			throw new IllegalStateException("sendError() cannot be completed. "
					+ "The response has already been committed.");
		}
		statusCode = code;
		resetBuffer();
		setContentType("text/html");
		if (getErrorText(code).length() > 0)
			writer.println(getErrorText(code));
		else writer.println("<html><body><div>" + message + "</div></body></html>");
		flushBuffer();
		committed = true;
	}

	@Override
	public void sendRedirect(String url) throws IOException {
		statusCode = 301;
		if (url.startsWith("http"))
			headers.put("Location", url);
		else if (url.startsWith("/")){
			StringBuilder fullPath = new StringBuilder("http://");
			fullPath.append(request.getHeader("Host"));
			fullPath.append(":");
			fullPath.append(request.getHeader("Port"));
			fullPath.append(url);
			headers.put("Location", fullPath.toString());
		}
		else {
			StringBuilder fullPath = new StringBuilder("http://");
			fullPath.append(request.getHeader("Host"));
			fullPath.append(":");
			fullPath.append(request.getHeader("Port"));
			fullPath.append(request.getRequestURI());
			fullPath.append(url);
			headers.put("Location", fullPath.toString());
		}	
		flushBuffer();
		committed = true;
	}

	@Override
	public void setDateHeader(String name, long value) {
		headers.put(name, value);
	}

	@Override
	public void setHeader(String name, String value) {
		headers.put(name, value);
	}

	@Override
	public void setIntHeader(String name, int intValue) {
		headers.put(name, new Integer(intValue));
		
	}

	@Override
	public void setStatus(int code) {
		statusCode = code;
		
	}
	
	/**
	 * Returns a default message string appropriate for the given error response code.
	 * @param code - error code.
	 * @return - String with default error messge.
	 */
	String getErrorText(int code) {
		switch(statusCode) {
		case(400):
			return "<html><body><div>400 Bad Request</div></body></html>";
		case(403):
			return "<html><body><div>403 Forbidden</div><br><div>You do not have access to that file.</div></body></html>";
		case(404):
			return "<html><body><div>404 Not Found</div></body></html>";
		case(500):
			return "<html><body><div>500 Internal Server Error</div></body></html>";
		default:
			return "";
		}
	}
	
//NOT IMPLEMENTED

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}
	
//DEPRECATED

	@Override
	public String encodeRedirectURL(String arg0) {
		return null;
	}

	@Override
	public String encodeURL(String arg0) {
		return null;
	}

	@Override
	public void setStatus(int arg0, String arg1) {}
}
