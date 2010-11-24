package com.verknowsys.served.kqueue;

import com.sun.jna.*;


public class Kqueue {
    public interface KqueueLib extends Library {
        public KqueueLib instance = (KqueueLib) Native.loadLibrary("kqueue", KqueueLib.class);
       
		public int kqueue_init();
		public void kqueue_close();
		public kevent kqueue_check(kevent change);
		public kevent kqueue_watch(String path);
    }

	protected static KqueueLib lib = KqueueLib.instance;

    public static void main(String[] args) {
		System.out.println("jna.librabry.path = " + System.getProperty("jna.library.path"));
		
		lib.kqueue_init();
		
		kevent watch = lib.kqueue_watch("/tmp/xx");
		while(true){
			System.out.println(".loop");
			kevent event = lib.kqueue_check(watch);
			if(event != null) System.out.println("fflags = " + event.fflags);
		}
		
		//lib.kqueue_close();
    }
}

