package com.verknowsys.served.kqueue;

import java.util.*;

public class KqueueFile {
	public String path;
	public kevent event;
	protected List<KqueueListener> listeners;
	
	public KqueueFile(String path, kevent event, KqueueListener listener){
		this.path = path;
		this.event = event;
		this.listeners = new ArrayList<KqueueListener>();
		addListener(listener);
	}
	
	public void addListener(KqueueListener listener){
		this.listeners.add(listener);
	}
	
	public void removeListener(KqueueListener listener){
	    this.listeners.remove(listener);
	}
	
	public boolean hasListeners(){
	    return !this.listeners.isEmpty();
	}
	
	public void call(){
		for(KqueueListener listener : listeners){
			listener.handle(); // TODO: Add some parameters
		}
	}
}