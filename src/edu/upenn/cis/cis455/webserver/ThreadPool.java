package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

/**
 * Creates a thread pool and provides methods to activate, deactivate and pass jobs.
 * @author Ryan Smith
 */
public class ThreadPool {
	ArrayList<RequestHandlerThread> threadList = new ArrayList<RequestHandlerThread>();
	static final Logger logger = Logger.getLogger(ThreadPool.class);
	Queue<Object> masterQueue;
	private int maxNumberOfRequests;
	private String rootDirectory;
	private boolean killSignal;
	private HashMap<String, HttpServlet> servlets;
	
	/**
	 * Constructor for thread pool.
	 * @param maxNumberOfRequests - Max number of requests that can be placed on the queue at one time.
	 * @param maxNumberOfHandlers - Number of threads to be started in the pool.
	 * @param rootDirectory - Directory from which content will be served.
	 */
	public ThreadPool(ServerConfiguration serverSettings) {
		masterQueue = new LinkedList<Object>();
		this.maxNumberOfRequests = serverSettings.maxNumberOfRequests;
		this.rootDirectory = serverSettings.rootDirectory;
		this.servlets = serverSettings.servlets;
		for (int i = 0; i < serverSettings.maxNumberOfHandlers; i++) {
			threadList.add(new RequestHandlerThread(this, masterQueue, serverSettings));
		}
		killSignal = false;
		logger.info("Created Thread Pool: " + new Date());
	}
	
	/**
	 * Starts all the threads in the thread pool so they will begin looking for requests in the queue.
	 */
	public void activate() {
		for (int i = 0; i < threadList.size(); i++) {
			(new Thread(threadList.get(i))).start();
		}
		logger.info("Thread Pool Activated: " + new Date());
	}
	
	/**
	 * Stops all the threads in the thread pool by telling them to stop looking 
	 * for requests in the queue. Destroys all servlets active in the server.
	 */
	public void deactivate() {
		for (int i = 0; i < threadList.size(); i++) {
			threadList.get(i).deactivate();
		}
		synchronized (masterQueue) {
			masterQueue.notifyAll();
		}
		for (String servletName : servlets.keySet()) {
			HttpServlet servlet = servlets.get(servletName);
			servlet.destroy();
		}
		logger.info("Thread Pool Deactivate Request Processed: " + new Date());
	}
	
	/**
	 * Adds a request to the queue for processing.
	 * @param request - Socket from the client.
	 * @throws InterruptedException
	 */
	public void submitForProcessing(Object request) throws InterruptedException {
		logger.info("Adding element to queue");
		while (masterQueue.size() == maxNumberOfRequests) {
			synchronized (masterQueue) {
				masterQueue.wait();
			}
		}
		//Add element to queue and notify all waiting consumers
		synchronized (masterQueue) {
			masterQueue.offer(request);
			masterQueue.notifyAll();
		}
	}
	
	//NOTE: making the following two method synchronized means that they will never be called at the same time
	/**
	 * Sets the killSignal variable to allow the threads to communicate that a /shutdown request has been found.
	 */
	public synchronized void notifyOfKillRequest() {
			killSignal = true;
	}

	/**
	 * Returns the current value of killSignal.
	 * @return
	 */
	public synchronized boolean getKillSignal() {
		return killSignal;
	}
	
}