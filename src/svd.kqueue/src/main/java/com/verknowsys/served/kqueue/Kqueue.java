package com.verknowsys.served.kqueue;

import com.sun.jna.*;




public class Kqueue {
    protected interface CLibrary extends Library {
		
        public CLibrary instance = (CLibrary) Native.loadLibrary("c", CLibrary.class);
       	// see <sys/event.h>
		public int kqueue();
		public int kevent(int kq, kevent changelist, int nchanges, kevent eventlist, int nevents, timespec timeout);
		public void perror(String label);
		public int open(String filename, int flags);
		
		public static final int EVFILT_READ		= -1;
		public static final int EVFILT_WRITE	= -2;
		public static final int EVFILT_AIO		= -3;	/* attached to aio requests */
		public static final int EVFILT_VNODE	= -4;	/* attached to vnodes */
		public static final int EVFILT_PROC		= -5;	/* attached to struct proc */
		public static final int EVFILT_SIGNAL	= -6;	/* attached to struct proc */
		public static final int EVFILT_TIMER	= -7;	/* timers */
		public static final int EVFILT_MACHPORT	= -8;	/* Mach portsets */
		public static final int EVFILT_FS		= -9;	/* Filesystem events */
		public static final int EVFILT_USER		= -10;   /* User events */
		public static final int	EVFILT_SESSION	= -11;	/* Audit session events */

		/* actions */
		public static final int EV_ADD		= 0x0001;		/* add event to kq implies enable) */
		public static final int EV_DELETE	= 0x0002;		/* delete event from kq */
		public static final int EV_ENABLE	= 0x0004;		/* enable event */
		public static final int EV_DISABLE	= 0x0008;		/* disable event not reported) */
		public static final int EV_RECEIPT	= 0x0040;		/* force EV_ERROR on success, data == 0 */

		/* flags */
		public static final int EV_ONESHOT	= 0x0010;		/* only report one occurrence */
		public static final int EV_CLEAR	= 0x0020;		/* clear event state after reporting */
		public static final int EV_DISPATCH = 0x0080;          /* disable event after reporting */

		public static final int EV_SYSFLAGS	= 0xF000;		/* reserved by system */
		public static final int EV_FLAG0	= 0x1000;		/* filter-specific flag */
		public static final int EV_FLAG1	= 0x2000;		/* filter-specific flag */

		/* returned values */
		public static final int EV_EOF		= 0x8000;		/* EOF detected */
		public static final int EV_ERROR	= 0x4000;		/* error, data contains errno */

		/*
		 * data/hint fflags for EVFILT_VNODE, shared with userspace
		 */
		public static final int	NOTE_DELETE	= 0x00000001;		/* vnode was removed */
		public static final int	NOTE_WRITE	= 0x00000002;		/* data contents changed */
		public static final int	NOTE_EXTEND	= 0x00000004;		/* size increased */
		public static final int	NOTE_ATTRIB	= 0x00000008;		/* attributes changed */
		public static final int	NOTE_LINK	= 0x00000010;		/* link count changed */
		public static final int	NOTE_RENAME	= 0x00000020;		/* vnode was renamed */
		public static final int	NOTE_REVOKE	= 0x00000040;		/* vnode access was revoked */
		public static final int NOTE_NONE	= 0x00000080;		/* No specific vnode event: to test for EVFILT_READ activation*/
		
		
		public static final int	O_RDONLY	= 0x0000;		/* open for reading only */
		public static final int O_WRONLY	= 0x0001;		/* open for writing only */
		public static final int	O_RDWR		= 0x0002;		/* open for reading and writing */
		public static final int	O_ACCMODE	= 0x0003;		/* mask for above modes */
	}

	protected static CLibrary clib = CLibrary.instance;

    public static void main(String[] args) {
		System.out.println("jna.librabry.path = " + System.getProperty("jna.library.path"));
		
		int kq = clib.kqueue();
		if(kq == -1){
			clib.perror("kqueue");
		}
		
		int f = clib.open("/tmp/xx", CLibrary.O_RDONLY);
		if(f == -1){
			clib.perror("open");
		}
		
		// #define EV_SET(kevp, a, b, c, d, e, f) do {	\
		// 	struct kevent *__kevp__ = (kevp);	\
		// 	__kevp__->ident = (a);			\
		// 	__kevp__->filter = (b);			\
		// 	__kevp__->flags = (c);			\
		// 	__kevp__->fflags = (d);			\
		// 	__kevp__->data = (e);			\
		// 	__kevp__->udata = (f);			\
		// } while(0)
		// EV_SET(&change, f, EVFILT_VNODE,
		// 	          EV_ADD | EV_ENABLE | EV_ONESHOT,
		// 	          NOTE_DELETE | NOTE_EXTEND | NOTE_WRITE | NOTE_ATTRIB,
		// 	          0, 0);
		
		kevent change = new kevent(new NativeLong(f), 
					(short)clib.EVFILT_VNODE,
					(short)(clib.EV_ADD | clib.EV_ENABLE | clib.EV_ONESHOT),
					clib.NOTE_DELETE | clib.NOTE_EXTEND | clib.NOTE_WRITE | clib.NOTE_ATTRIB,
					new NativeLong(), Pointer.NULL);
		
		kevent event = new kevent();
		
		while(true){
			int nev = clib.kevent(kq, change, 1, event, 1, null);
			if(nev == -1){
				clib.perror("kevent");
			} else if(nev > 0) {
				if ((event.fflags & clib.NOTE_DELETE) != 0) {
	               System.out.println("File deleted");
	               break;
	           }
	           if ((event.fflags & clib.NOTE_EXTEND) != 0 || (event.fflags & clib.NOTE_WRITE) != 0)
	               System.out.println("File modified");
	           if ((event.fflags & clib.NOTE_ATTRIB) != 0)
	               System.out.println("File attributes modified");
			}
		}
		
		
		// public kevent(NativeLong ident, short filter, short flags, int fflags, NativeLong data, Pointer udata) {
		
		// Thread a = new Thread(){
		// 	public void run(){
		// 		kevent watch = lib.kqueue_watch("/tmp/aa");
		// 		if(watch != null){
		// 			while(true){
		// 				// System.out.println("a.loop");
		// 				kevent event = lib.kqueue_check(watch);
		// 				if(event != null) System.out.println("a.fflags = " + event.fflags);
		// 			}
		// 		}
		// 
		// 	}
		// };
		// 
		// Thread b = new Thread(){
		// 	public void run(){
		// 		kevent watch = lib.kqueue_watch("/tmp/bb");
		// 		if(watch != null){
		// 			while(true){
		// 				// System.out.println("b.loop");
		// 				kevent event = lib.kqueue_check(watch);
		// 				if(event != null) System.out.println("b.fflags = " + event.fflags);
		// 			}
		// 		}
		// 	}
		// };
		// 
		// a.start();
		// b.start();

		
		//lib.kqueue_close();
    }
}

