package com.verknowsys.served.kqueue;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;


// mvnclean compile exec:java -Dexec.mainClass=com.verknowsys.served.kqueue.Kqueue
 
public class Kqueue {
    public interface KqueueLib extends Library {
        public KqueueLib instance = (KqueueLib) Native.loadLibrary("c", KqueueLib.class);
        public void printf(String format, Object... args);
    }
 
    public static void main(String[] args) {
		KqueueLib kqueue = KqueueLib.instance;
	
		args = new String[]{"1", "2", "3", "4"};
        kqueue.printf("Hello, World\n");
        for (int i = 0; i < args.length; i++) {
            kqueue.printf("Argument %d: %s\n", i, args[i]);
        }
    }
}
