package edu.upenn.cis.cis455.webserver;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

public class SyncQueue {
	int maxCapacity;
	Queue<Object> queue = new LinkedList<Object>();
	static final Logger logger = Logger.getLogger(SyncQueue.class);
	
	public SyncQueue(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}
	public synchronized void offer(Object o) throws InterruptedException{
		while (queue.size() == maxCapacity) {
				wait();
		}
		if (queue.size() != maxCapacity) { //this line may not be necessary
			notifyAll();
		}
		queue.offer(o);
	}
	
	public synchronized Object poll() throws InterruptedException {
		while (queue.size() == 0) {
		//if (queue.size() == 0)
			wait();
		}
		if (queue.size() > 0) { //this line may not be necessary
			notify();
		}
		return queue.poll();
	}

}
