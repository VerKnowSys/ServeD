package com.verknowsys.served.kqueue;

import com.sun.jna.*;


public class Kqueue {
    protected interface KqueueLib extends Library {
		// see <sys/event.h>
        public KqueueLib instance = (KqueueLib) Native.loadLibrary("kqueue", KqueueLib.class);
       
		public int kqueue_init();
		public void kqueue_close();
		public kevent kqueue_check(kevent change);
		public kevent kqueue_watch(String path);
		
		public static final int	NOTE_DELETE	= 0x01;		/* vnode was removed */
		public static final int	NOTE_WRITE	= 0x02;		/* data contents changed */
		public static final int	NOTE_EXTEND	= 0x04;		/* size increased */
		public static final int	NOTE_ATTRIB	= 0x08;		/* attributes changed */
		public static final int	NOTE_LINK	= 0x10;		/* link count changed */
		public static final int	NOTE_RENAME	= 0x20;		/* vnode was renamed */
		public static final int	NOTE_REVOKE	= 0x40;		/* vnode access was revoked */
		public static final int NOTE_NONE	= 0x80;		/* No specific vnode event: to test for EVFILT_READ activation*/
    }

	protected static KqueueLib lib = KqueueLib.instance;

    public static void main(String[] args) {
		System.out.println("jna.librabry.path = " + System.getProperty("jna.library.path"));
		
		lib.kqueue_init();
		
		Thread a = new Thread(){
			public void run(){
				kevent watch = lib.kqueue_watch("/tmp/aa");
				if(watch != null){
					while(true){
						// System.out.println("a.loop");
						kevent event = lib.kqueue_check(watch);
						if(event != null) System.out.println("a.fflags = " + event.fflags);
					}
				}

			}
		};
		
		Thread b = new Thread(){
			public void run(){
				kevent watch = lib.kqueue_watch("/tmp/bb");
				if(watch != null){
					while(true){
						// System.out.println("b.loop");
						kevent event = lib.kqueue_check(watch);
						if(event != null) System.out.println("b.fflags = " + event.fflags);
					}
				}
			}
		};
		
		a.start();
		b.start();

		
		//lib.kqueue_close();
    }
}

