package com.verknowsys.served.kqueue;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;


// mvnclean compile exec:java -Dexec.mainClass=com.verknowsys.served.kqueue.Kqueue
 
public class Kqueue {
    public interface KqueueLib extends Library {
        public KqueueLib instance = (KqueueLib) Native.loadLibrary("kqueue", KqueueLib.class);
        public int dupa();
    }

	protected static KqueueLib kqueue = KqueueLib.instance;
 
    public static void main(String[] args) {
		System.out.println("jna.librabry.path = " + System.getProperty("jna.library.path"));
		System.out.println(kqueue.dupa());
    }
}
