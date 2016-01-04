package edu.upenn.cis.cis455.webserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import javax.servlet.http.HttpServlet;
import org.apache.log4j.Logger;

/**
 * Multi-threaded Http Server for static content. 
 * 
 * @author Ryan Smith
 *
 *
 */
class HttpServer {
	static Logger logger;

  /**
   * Main method. Runs the program.
   * @param args - command line args
   */
  public static void main(String args[])
  {
	  try { 
		  if (args.length != 3) {
			  System.out.println("Ryan Smith\nrysmit");
		  }
		  else {
			  String port = args[0];
			  String webRoot = args[1];
			  String webDotXMLPath = args[2];
			  logger = Logger.getLogger(HttpServer.class);
			  logger.error("Test error to display during grading in case no other errors are thrown.");
			  
			  //servlet setup
			  ServletCreator.Handler handler = ServletCreator.parseWebDotXML(webDotXMLPath);
			  ServletContextClone context = ServletCreator.createContext(handler, webRoot, port);
			  HashMap<String, HttpServlet> servlets = ServletCreator.createServlets(handler, context);
			  HashMap<String, String> urlRoutingMap = handler.urlRoutingMap;
			  HashMap<String, HttpSessionClone> sessionsReference = new HashMap<String, HttpSessionClone>();
			  ServerConfiguration serverSettings = 
					  new ServerConfiguration(servlets, urlRoutingMap, sessionsReference, 10000, 10, webRoot, context);
			  
			  //server setup
			  ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
			  ThreadPool tPool = new ThreadPool(serverSettings);
			  tPool.activate();
			  Socket clientSocket = null;
			  Socket lastClientSocket = null;
			  while(!tPool.getKillSignal()){  
				  serverSocket.setSoTimeout(5000); //server will stop waiting every 5 seconds, this allows it to recheck boolean
				  try{
					  clientSocket =  serverSocket.accept();
				  }
				  catch(Exception e){/*allow looping to continue*/}
				  if (((clientSocket != null) && (clientSocket != lastClientSocket))){
					  tPool.submitForProcessing(clientSocket);
					  lastClientSocket = clientSocket;
				  }
				  		
			  }
			  tPool.submitForProcessing(clientSocket); //not sure why this needs to be here but shutdown will hang without
			  tPool.deactivate();
			  serverSocket.close();
		  }
	  } catch (Exception e){
		  e.printStackTrace();
		  //logger.info("An exception occurred that could not be handled by the threads. Please restart.");
	  }
  }
}