package com.verknowsys.served.utils;

import com.sun.jna.*;


/**
 *  @author teamon, dmilith
 *
 *   Interfaces and libraries to low level access to posix system functions
 */ 

public interface CLibrary extends Library {
    public static final CLibrary instance = (CLibrary) Native.loadLibrary("c", CLibrary.class);

    // 2011-01-26 00:47:55 - dmilith - NOTE: some SvdProcess requirements:
    public int kill(long pid, int signal);
    public int chmod(String filename, int mode);
    public int chown(String filename, int user, int group);
    public int getuid();
    
    // see sys/event.h header file
    public int kqueue();
    public int kevent(int kq, kevent change, int nchanges, kevent event, int nevents, Pointer timeout);
    public void perror(String label);
    public int open(String filename, int flags);
    public void close(int fd);
    
    public static final int NOTE_DELETE = 0x00000001;       /* vnode was removed */
    public static final int NOTE_WRITE  = 0x00000002;       /* data contents changed */
    public static final int NOTE_EXTEND = 0x00000004;       /* size increased */
    public static final int NOTE_ATTRIB = 0x00000008;       /* attributes changed */
    public static final int NOTE_LINK   = 0x00000010;       /* link count changed */
    public static final int NOTE_RENAME = 0x00000020;       /* vnode was renamed */
    public static final int NOTE_REVOKE = 0x00000040;       /* vnode access was revoked */
    public static final int NOTE_NONE   = 0x00000080;       /* No specific vnode event: to test for EVFILT_READ activation*/
    
    public static final int O_RDONLY    = 0x0000;       /* open for reading only */
    public static final int O_WRONLY    = 0x0001;       /* open for writing only */
    public static final int O_RDWR      = 0x0002;       /* open for reading and writing */
    public static final int O_ACCMODE   = 0x0003;       /* mask for above modes */

    public static final int EVFILT_READ     = -1;
    public static final int EVFILT_WRITE    = -2;
    public static final int EVFILT_AIO      = -3;   /* attached to aio requests */
    public static final int EVFILT_VNODE    = -4;   /* attached to vnodes */
    public static final int EVFILT_PROC     = -5;   /* attached to struct proc */
    public static final int EVFILT_SIGNAL   = -6;   /* attached to struct proc */
    public static final int EVFILT_TIMER    = -7;   /* timers */
    public static final int EVFILT_MACHPORT = -8;   /* Mach portsets */
    public static final int EVFILT_FS       = -9;   /* Filesystem events */
    public static final int EVFILT_USER     = -10;  /* User events */
    public static final int EVFILT_SESSION  = -11;  /* Audit session events */

    /* actions */
    public static final int EV_ADD      = 0x0001;       /* add event to kq implies enable) */
    public static final int EV_DELETE   = 0x0002;       /* delete event from kq */
    public static final int EV_ENABLE   = 0x0004;       /* enable event */
    public static final int EV_DISABLE  = 0x0008;       /* disable event not reported) */
    public static final int EV_RECEIPT  = 0x0040;       /* force EV_ERROR on success, data == 0 */

    /* flags */
    public static final int EV_ONESHOT  = 0x0010;       /* only report one occurrence */
    public static final int EV_CLEAR    = 0x0020;       /* clear event state after reporting */
    public static final int EV_DISPATCH = 0x0080;          /* disable event after reporting */

    public static final int EV_SYSFLAGS = 0xF000;       /* reserved by system */
    public static final int EV_FLAG0    = 0x1000;       /* filter-specific flag */
    public static final int EV_FLAG1    = 0x2000;       /* filter-specific flag */

    /* returned values */
    public static final int EV_EOF      = 0x8000;       /* EOF detected */
    public static final int EV_ERROR    = 0x4000;       /* error, data contains errno */
}

