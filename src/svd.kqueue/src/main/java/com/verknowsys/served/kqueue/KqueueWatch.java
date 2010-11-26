package com.verknowsys.served.kqueue;

public class KqueueWatch {
    public Kqueue queue;
	public KqueueFile file;
	public KqueueListener listener;
	
	public KqueueWatch(Kqueue queue, KqueueFile file, KqueueListener listener){
	    this.queue = queue;
	    this.file = file;
	    this.listener = listener;
	}
	
	public void stop(){
	    file.removeListener(listener);
	    if(!file.hasListeners()){
	        queue.removeFile(file);
	    }
	}
}
