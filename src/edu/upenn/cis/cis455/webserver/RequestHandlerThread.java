package edu.upenn.cis.cis455.webserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Queue;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

/**
 * Single thread HTTP request processor. Participates as part of thread pool.
 * Loops and serves requests until thread pool deactivates.
 * @author Ryan Smith
 */
public class RequestHandlerThread implements Runnable {
	
	static final Logger logger = Logger.getLogger(RequestHandlerThread.class);
	private ThreadPool master;
	private Queue<Object> masterQueue;
	private boolean active;
	private ServerConfiguration serverSettings;
	String currentURL; // just used to allow the monitor page to view
	
	/**
	 * Constructor for the thread.
	 * @param masterQueue - reference to the shared queue where client connections will wait for processing.
	 * @param rootDirectory - the directory being served by the server.
	 */
	public RequestHandlerThread(ThreadPool master, 
			Queue<Object> masterQueue, ServerConfiguration serverSettings) {
		this.master = master;
		this.masterQueue = masterQueue;
		this.active = true;
		this.serverSettings = serverSettings;
	}
	
	/**
	 * Starts the execution of the thread. Continues to look for clients that need processing in the 
	 * queue until being deactivated.
	 */
	@Override
	public void run() {
		logger.info("RequestHandlerThread " + this + " acknowledged was activated: " + new Date());
		Socket clientSocket = null;
		try {
			while (isActive()) {
				Object retrievedObject = readFromQueue();
				if (retrievedObject != null){
					clientSocket =  (Socket) retrievedObject;
					logger.info("RequestHandlerThread " + this + "received a Socket " 
							+ clientSocket + " for processing: " +new Date());
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					HashMap<String, String> requestDetails = parseRequest(in);
					requestDetails.put("FullPath", cleanPath(requestDetails.get("FullPath")));
					currentURL = removeQueryStringIfPresent(requestDetails.get("FullPath"));
					HttpServlet matchedServlet = searchPathMappings(requestDetails);
					if (matchedServlet != null) {
						startServlet(matchedServlet, clientSocket, requestDetails);
					}
					else { //handle static content requests
						String path = serverSettings.rootDirectory + requestDetails.get("FullPath");
						byte[] response = constructResponse(checkFilePath(path), path, requestDetails);	
				        sendResponse(clientSocket, response);
					}
			        in.close();
			        clientSocket.close();
					logger.info("RequestHandlerThread " + this + " finished processing Socket " + 
							clientSocket + ": " + new Date());
				}
			}
			logger.info(" shutting down");
			return;
		} catch (Exception ex) { //catches any exceptions that occur during processing and attempts to send 500
			try {
				//logger.error("An exception occurred. Possibly bad socket.");
		        ex.printStackTrace();
				sendResponse(clientSocket, constructErrorResponse(500));
		        clientSocket.close();
			} catch (Exception e2) { //catches any exceptions that occur while sending 500
				logger.error("Unable to send 500 following exception. ", e2);
			}
		}
	}
	String removeQueryStringIfPresent(String path) {
		if (path.indexOf("?") > -1)
			return path.substring(0, path.indexOf("?"));
		return path;
	}
	/**
	 * Creates an HttpServletRequestClone and HttpServletResponseClone and calls the service
	 * method of the provided servlet.
	 * @param servlet - the servlet that will serve this request.
	 * @param clientSocket - the socket with connection to the client.
	 * @param requestDetails - Map containing headers from the request.
	 * @throws Exception
	 */
	public void startServlet(HttpServlet servlet, Socket clientSocket,
			HashMap<String, String> requestDetails) throws Exception {
		HashMap<String, String> cookiesMap = makeCookiesMap(requestDetails);
		HttpServletRequestClone request = new HttpServletRequestClone(serverSettings.sessionsReference, 
				serverSettings.servletContext, cookiesMap, requestDetails, clientSocket);
		request.setAttribute("method", requestDetails.get("Type"));
		request.setAttribute("regexMatch", requestDetails.get("regexMatch"));
		requestDetails.remove("regexMatch");
		parseParameters(request, requestDetails);
		HttpServletResponseClone response = new HttpServletResponseClone(clientSocket.getOutputStream(), request);
		request.addResponseClone(response);
		servlet.service(request, response);
		if (!response.committed)
			response.flushBuffer();
	}
	
	/**
	 * Parses the parameter of an HTTP request and adds them to an HttpServletRequestClone's 
	 * parameter map.
	 * @param request - the HttpServletRequestClone representing this request.
	 * @param requestDetails - Map containing headers from the http request.
	 */
	private void parseParameters(HttpServletRequestClone request, HashMap<String, String> requestDetails) {
		if (requestDetails.get("Type").equals("GET")){
			if (requestDetails.get("FullPath").contains("?")) {
				String URInoQuery = requestDetails.get("FullPath").substring(0, requestDetails.get("FullPath").indexOf("?"));
				request.setAttribute("URI", URInoQuery);
				String queryString = requestDetails.get("FullPath").substring(requestDetails.get("FullPath").indexOf("?"));
				request.setAttribute("queryString", queryString);
				String[] pieces = queryString.split("&|=");
				for (int i = 0; i < pieces.length - 1; i += 2) {
					request.setParameter(pieces[i], pieces[i+1]);
				}
			}
			else {
				request.setAttribute("URI", requestDetails.get("FullPath"));
			}
		}
		else if (requestDetails.get("Type").equals("POST")) {
			request.setAttribute("URI", requestDetails.get("FullPath"));
			String body = requestDetails.get("body");
			if (body != null) {
				if (body.matches(".+=.+[&.+=.+]*")){
					String[] pieces = body.split("&|=");
					for (int i = 0; i < pieces.length - 1; i += 2) {
						request.setParameter(pieces[i], pieces[i+1]);
					}
				}
				request.setAttribute("body", requestDetails.get("body"));
				requestDetails.remove("body");
				logger.error("handler test error");
			}
		}
	}
	
	/**
	 * Sets active to false to indicate that the thread should stop looking for
	 * connections and exit.
	 */
	public synchronized void deactivate() {
			active = false;
	}

	/**
	 * Allows the thread to check if it should continue looping in a synchronized way.
	 * @return
	 */
	public synchronized boolean isActive() {
		return active;
	}
	//NOTE: making the above two method synchronized means that they will never be called at the same time
	
	/**
	 * Opens an OutputStream and sends a full response to the client.
	 * @param clientSocket - Socket connection to the client.
	 * @param response - byte[] with the response.
	 * @throws IOException
	 */
	public void sendResponse(Socket clientSocket, byte[] response) throws IOException {
		OutputStream outStream = clientSocket.getOutputStream();
		outStream.write(response,0,response.length);
		outStream.flush();
		outStream.close();
	}
	
	/**
	 * Removes any characters that may have been encoded during transmission and replaces
	 * them with their regular values. (Right now this only looks for spaces but can be easily expanded).
	 * @param path - String containing the relative path of the item sought.
	 * @return path - String with the encoded characters replaced.
	 */
	public String cleanPath(String path) {
		while (path.indexOf("%20") >= 0) {
			path = path.replace("%20", " ");
		}
		return path;
	}
	
	/**
	 * Constructs a response to the client based on arguments.
	 * @param fileStatus - status of file (exists/doesn't, readable/locked)
	 * @param path - path to requested file
	 * @param requestDetails - parsed request details from the client
	 * @return byte[] containing full response to be transmitted.
	 * @throws IOException
	 */
	public byte[] constructResponse(int fileStatus, String path, HashMap<String, String> requestDetails) throws IOException {
		if (requestDetails.get("Host") == null)
			return constructErrorResponse(400);
		byte[] headers;
		switch (fileStatus) {
			case(0):
				if (requestDetails.get("If-Modified-Since") != null) {
					if (!checkModifiedDate(path, requestDetails.get("If-Modified-Since"), 0))
						return constructErrorResponse(304);
				}
				if (requestDetails.get("If-Unmodified-Since") != null) {
					if (!checkModifiedDate(path, requestDetails.get("If-Unmodified-Since"), 1))
						return constructErrorResponse(412);
				}
				byte[] resource = fetchResource(path);
				headers = createSuccessHeaders(checkFileType(path), resource.length, path);
				if(requestDetails.get("Type").equals("GET"))
					return concatByteArrays(headers, resource);
				else if (requestDetails.get("Type").equals("HEAD"))
					return headers;
			case(1):
				byte[] listingHTML = constructDirectoryListing(path);
				headers = createSuccessHeaders("directory", listingHTML.length, path);
				if(requestDetails.get("Type").equals("GET"))
					return concatByteArrays(headers, listingHTML);
				else if (requestDetails.get("Type").equals("HEAD"))
					return headers;
			case(404):
				return constructErrorResponse(404);
			case(403):
				return constructErrorResponse(403);
			case(-1):
				master.notifyOfKillRequest();
				return constructErrorResponse(-1);
			case(-2):
				byte[] panel = createControlPanel();
				headers = createSuccessHeaders("control", panel.length, path);
				return concatByteArrays(headers, panel);
		}
		return constructErrorResponse(500); //function should never reach here, send error if so
	}
	
	/**
	 * Attempts to parse date in all three formats specified by HTTP Made Really Easy.
	 * Sends resource if response cannot be parsed.
	 * @param path - path to resource
	 * @param dateString - date for constraint
	 * @param method - 0 for if-modified-since, 1 for if-unmodified-since
	 * @return true if resources should be send according to request condition, false if not.
	 */
	public boolean checkModifiedDate(String path, String dateString, int method) {
		File file = new File(path);
		Calendar modTime = Calendar.getInstance();
		modTime.setTimeInMillis(file.lastModified());
		Date requestDate;
		try {
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
			requestDate = format.parse(dateString);
		} catch (ParseException e0) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("EEE, dd-MMM-yy HH:mm:ss zzz");
				requestDate = format.parse(dateString);
			}catch (ParseException e1) {
				try {
					SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
					requestDate = format.parse(dateString);
				}catch (ParseException e2) {
					requestDate = null;
				}
			}
		}
		if (requestDate == null)
			return true;
		Calendar requestTime = Calendar.getInstance();
		requestTime.setTime(requestDate);
		int status = requestTime.compareTo(modTime);
		if (status < 0 && method == 0)
			return true;
		if (status >= 0 && method == 0)
			return false;
		if (status < 0 && method == 1)
			return false;
		if (status >= 0 && method == 1)
			return true;
		return true;
	}
	
	/**
	 * Constructs an appropriate response in the case that an error is encountered. 
	 * Currently supports 400, 403, 404 and 500 but can be extended.
	 * @param statusCode - indicates the error response that should be constructed.
	 * @return byte[] containing full error response.
	 */
	public byte[] constructErrorResponse(int statusCode) {
		String response;
		switch(statusCode) {
			case(400):
				response = "HTTP/1.0 400 Bad Request\r\nConnection: close\r\nContent-Type: text/html\r\n" +
						"Date: " + new Date() + "\r\n\r\n<html><body><div>400 Bad Request</div></body></html>";
				return response.getBytes(Charset.forName("UTF-8"));
			case(403):
				response = "HTTP/1.0 403 Forbidden\r\nConnection: close\r\nContent-Type: text/html\r\n"+
						"Date: " + new Date() + "\r\n\r\n<html><body><div>403 Forbidden</div><br><div>You do not have access to that file.</div></body></html>";
				return response.getBytes(Charset.forName("UTF-8"));
			case(404):
				response = "HTTP/1.0 404 Not Found\r\nConnection: close\r\nContent-Type: text/html\r\n"+
						"Date: " + new Date() + "\r\n\r\n<html><body><div>404 Not Found</div></body></html>";
				return response.getBytes(Charset.forName("UTF-8"));
			case(500):
				response = "HTTP/1.0 500 Server Error\r\nConnection: close\r\nContent-Type: text/html\r\n"+
						"Date: " + new Date() + "\r\n\r\n<html><body><div>500 Internal Server Error</div></body></html>";
				return response.getBytes(Charset.forName("UTF-8"));	
			case(304):
				response = "HTTP/1.1 304 Not Modified\r\nConnection: close\r\nContent-Type: text/html\r\n"+
						"Date: " + new Date() + "\r\n\r\n";
				return response.getBytes(Charset.forName("UTF-8"));	
			case(412):
				response = "HTTP/1.1 412 Precondition Failed\r\nConnection: close\r\nContent-Type: text/html\r\n"+
						"Date: " + new Date() + "\r\n\r\n";
				return response.getBytes(Charset.forName("UTF-8"));	
			case(-1):
				response = "HTTP/1.0 200 OK\r\nConnection: close\r\nContent-Type: text/html\r\n"+
						"Date: " + new Date() + "\r\n\r\n<html><body><div>Shutdown Request Received</div></body></html>";
				return response.getBytes(Charset.forName("UTF-8"));				
		}
		return null;
	}
	
	/**
	 * Constructs html listing directory contents in the case that the requested path points to a directory.
	 * @param path - requested path
	 * @return byte[] containing response
	 */
	public byte[] constructDirectoryListing(String path) {
		ArrayList<File> files = new ArrayList<File>(Arrays.asList(new File(path).listFiles()));
		String response = "<html><body><div>Contents of " + path + "</div><br><div><ul>";
		for (File file : files) {
			response += "<li><a href=\"" + file.getName() + "\">" + file.getName() + "</a></li><br>";
		}
		response += "</div></body></html>";
		return response.getBytes(Charset.forName("UTF-8"));	
	}
	
	/**
	 * Creates response headers for a successful request. 
	 * (Unsuccessful requests have headers appended automatically in createErrorResponse())
	 * @param fileType - extension of file being transmitted.
	 * @param contentLength - length of content being transmitted.
	 * @return
	 */
	public byte[] createSuccessHeaders(String fileType, int contentLength, String path) {
		File currentFile = new File(path);		
		String response = "HTTP/1.0 200 OK\r\n";
        response += "Date: " + new Date() + "\r\n";
        response += "Connection: close\r\n";
        response += "Content-Length: " + contentLength + "\r\n";
        response += "Last-Modified: " + new Date(currentFile.lastModified()) + "\r\n";
        switch(fileType){
        case(".jpg"):
        	response += "Content-Type: image/jpeg\r\n\r\n";
        	break;
        case(".gif"):
        	response += "Content-Type: image/gif\r\n\r\n";
        	break;
        case(".png"):
        	response += "Content-Type: image/png\r\n\r\n";
        	break;
        case(".txt"):
        	response += "Content-Type: text/plain\r\n\r\n";
        	break;
        case("control"):
        case("directory"):
        case(".html"):
        	response += "Content-Type: text/html\r\n\r\n";
        	break;
        case(".js"):
        	response += "Content-Type: application/javascript\r\n\r\n";
        	break;
        case(".json"):
        	response += "Content-Type: application/json\r\n\r\n";
        	break;
        case(".pdf"):
        	response += "Content-Type: application/pdf\r\n\r\n";
        	break;
        }
        return response.getBytes(Charset.forName("UTF-8"));
		
	}
	
	/**
	 * Reads and return a resource from the directory in byte form.
	 * @param path - path to requested resource.
	 * @return the requested resource in byte[] form.
	 * @throws IOException
	 */
	public byte[] fetchResource(String path) throws IOException {
		File myFile = new File (path);
        byte [] resourceBytes  = new byte [(int)myFile.length()];
        FileInputStream fileStream = new FileInputStream(myFile);
        BufferedInputStream bufStream = new BufferedInputStream(fileStream);
        bufStream.read(resourceBytes,0,resourceBytes.length);
        bufStream.close();
        return resourceBytes;
	}
	
	/**
	 * Parses HTTP Request headers and stores in a HashMap.
	 * @param in - connection to client in stream.
	 * @return HashMap containing request details.
	 * @throws IOException
	 */
	public HashMap<String,String> parseRequest(BufferedReader in) throws IOException {
		HashMap<String, String> requestDetails = new HashMap<String, String>();	
		String inputLine = in.readLine();
        boolean first = true;
        while (!inputLine.equals("")){
        	//parse first line request details positionally
            if (first){
            	inputLine = inputLine.trim();
            	String[] firstLineComponents = inputLine.split(" ");
            	requestDetails.put("Type", firstLineComponents[0]);
            	requestDetails.put("FullPath", firstLineComponents[1]);
            	requestDetails.put("Protocol", firstLineComponents[2]);
            	first = false;
            }
            else {
            	//Parse Cookies line
            	if (inputLine.trim().startsWith("Cookie")){
            		inputLine = inputLine.substring(inputLine.indexOf(":") + 1).trim();
            		String[] cookies = inputLine.split("; ");
            		for (int i = 0; i < cookies.length; i++){
            			String[] pieces = cookies[i].split("=");
            			if (pieces.length == 2)
            				requestDetails.put("COOKIE:" + pieces[0].trim(), pieces[1].trim());
            		}
            	}
            	//separate and add host and port
            	else if (inputLine.trim().startsWith("Host")) {
            		String[] ipAndPort = inputLine.split(":");
                	requestDetails.put("Host", ipAndPort[1].trim());
                	requestDetails.put("Port", ipAndPort[2].trim());
            	}
            	else{
            		//parse remaining header lines
            		requestDetails.put(inputLine.substring(0, inputLine.indexOf(":")).trim(), 
            			inputLine.substring(inputLine.indexOf(":") + 1).trim());
            	}
            }
            inputLine = in.readLine();
        }
        //parses post body if available
        StringBuilder body = new StringBuilder();
        if (requestDetails.get("Type").equals("POST")){
        	int contentLength = Integer.parseInt(requestDetails.get("Content-Length"));
        	int c = 0;
            for (int i = 0; i < contentLength; i++) {
                c = in.read();
                body.append((char) c);
            }
        }
        if (body.length() > 0)
        	requestDetails.put("body", body.toString());
        return requestDetails;
	}
	
	/**
	 * Synchronizes on the masterQueue and attempts to return a client Socket to this thread.
	 * If the masterQueue is empty or unavailable, thread will be asked to wait until being notified.
	 * @return Socket with connection to the client.
	 * @throws InterruptedException
	 */
	private Object readFromQueue() throws InterruptedException {
		while (masterQueue.isEmpty()) {
			//If the queue is empty, we push the current thread to waiting state. Way to avoid polling.
			synchronized (masterQueue) {
				masterQueue.wait();
			}
		}
		if (isActive()){
			//Otherwise consume element and notify waiting producer
			synchronized (masterQueue) {
				masterQueue.notifyAll();
				return masterQueue.poll();
			}
		}
		return null;
	}
	
	/**
	 * Interrogates the requested path to determine is file exists/is available/etc.
	 * @param path - path to requested resource
	 * @return
	 */
	public int checkFilePath(String path) {
		File file = new File(path);
		if (path.substring(path.lastIndexOf("/")).equals("/shutdown"))
			return -1;
		if (path.substring(path.lastIndexOf("/")).equals("/control"))
			return -2;
		if (!file.exists())
			return 404;
		if (file.isDirectory())
			return 1;
		if (!file.canRead())
			return 403;
		return 0;
	}
	
	/**
	 * Fetches the extension of a requested resource from the resource's path.
	 * @param path - path to requested resource.
	 * @return
	 */
	public String checkFileType(String path) {
		String extension = path.substring(path.lastIndexOf("."));
		extension = extension.replace("/", "");
		return extension;
	}
	
	/**
	 * Concatenates two byte arrays.
	 * @param a - first byte[]
	 * @param b - second byte[]
	 * @return byte[] containing (a + b)
	 */
	public byte[] concatByteArrays(byte[] a, byte[] b) {
		int aLen = a.length;
		int bLen = b.length;
		byte[] c= new byte[aLen+bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}
	
	/**
	 * Creates a response containing the control panel.
	 * @return response in byte[] form.
	 * @throws IOException 
	 */
	public byte[] createControlPanel() throws IOException{
		String response = "<html><body><h1>Ryan Smith<br>rysmit</h1><div><h1>Thread States</h1><ul>";
		for (RequestHandlerThread thread : master.threadList) {
			response += "<li>" + (new Thread(thread).getName()) + ",      Processing :  ";
			if ((new Thread(thread).getState()) == Thread.State.RUNNABLE)
				response += currentURL + "</li>";
			else
				response += (new Thread(thread).getState()) + "</li>";
		}
		response += "</ul><a href=\"/shutdown\">Shutdown</a></div>";
		//add error logs from errorlog.log
		File errorLog = new File(System.getProperty("user.dir") + "/logs/errorlog.log");
		BufferedReader errorLogReader = new BufferedReader(new FileReader(errorLog));
		String line;
		ArrayList<String> errorStrings = new ArrayList<String>();
		while ((line = errorLogReader.readLine()) != null) {
            errorStrings.add(line);
        }
		response += "<h1>Error Log</h1><div><ul>";
		for (String singleError : errorStrings) {
			response += "<li>" + singleError + "</li>";
		}
		response += "</ul></div></body></html>";
		return response.getBytes(Charset.forName("UTF-8"));	
	}
	
	public HashMap<String, String> makeCookiesMap(HashMap<String, String> requestDetails) {
		HashMap<String, String> cookies = new HashMap<String, String>();
		for (String key : requestDetails.keySet()) {
			if (key.startsWith("COOKIE")) {
				String value = requestDetails.get(key);
				String name = key.substring(key.indexOf(":") + 1);
				cookies.put(name, value);
			}
		}
		return cookies;
	}
	
	/**
	 * Constructs a Regular Expression for each URL routing and attempts to match
	 * to the requested URL. Returns the appropriate servlet object if a match is found
	 * and null otherwise.
	 * @param path - requested path
	 * @return - servlet matching the requested URL or null if none found
	 */
	public HttpServlet searchPathMappings(HashMap<String, String> requestDetails) {
		String path = removeQueryStringIfPresent(requestDetails.get("FullPath"));
		for (String key : serverSettings.urlRoutingMap.keySet()) {
			StringBuilder regexKey = new StringBuilder();
			for (int i = 0; i < key.length(); i++) {
				char c = key.charAt(i);
				if (i == 0 && c != '/')
					regexKey.append('/');
				if (c == '.')
					regexKey.append("\\.");
				else if (c == '*')
					regexKey.append(".*");
				else
					regexKey.append(c);
			}
			//first two conditions allow for absence of trailing slash in URL
			if (regexKey.toString().endsWith("/")) {
				if (path.matches(regexKey.substring(0, regexKey.length() - 1))) {
					requestDetails.put("regexMatch", regexKey.toString());
					return serverSettings.servlets.get(serverSettings.urlRoutingMap.get(key));
				}
			}
			if (regexKey.toString().endsWith("/.*")) {
				if (path.matches(regexKey.substring(0, regexKey.length() - 3))) {
					requestDetails.put("regexMatch", regexKey.toString());
					return serverSettings.servlets.get(serverSettings.urlRoutingMap.get(key));
				}
			}
			if (path.matches(regexKey.toString())) {
				requestDetails.put("regexMatch", regexKey.toString());
				return serverSettings.servlets.get(serverSettings.urlRoutingMap.get(key));
			}
		}
		return null;
	}
}
