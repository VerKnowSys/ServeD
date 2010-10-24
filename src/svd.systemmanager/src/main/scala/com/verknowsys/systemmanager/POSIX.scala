// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.sun.jna.Library



/**
*   @author dmilith
*   
*   Class which describe any system process
*/
class SystemProcess(
    val processName: String = "root",
    val pid: String = "0"
) {
    override def toString = "PNAME: %s, PID: %s. ".format(processName, pid)
}


/**
*   @author dmilith
*   
*   POSIXSignals enum definition
*/
object POSIXSignals extends Enumeration(initial = 1) {
    type POSIXSignals = Value
    @specialized val SIGHUP, // 2010-10-05 15:03:27 - dmilith - NOTE: should have value 1
        SIGINT,
        SIGQUIT,
        SIGILL,
        SIGTRAP,
        SIGABRT,
        SIGBUS,
        SIGFPE,
        SIGKILL,
        SIGUSR1,
        SIGSEGV,
        SIGUSR2,
        SIGPIPE,
        SIGALRM,
        SIGTERM,
        SIGSTKFLT,
        SIGCHLD,
        SIGCONT,
        SIGSTOP,
        SIGTSTP,
        SIGTTIN,
        SIGTTOU,
        SIGURG,
        SIGXCPU,
        SIGXFSZ,
        SIGVTALRM,
        SIGPROF,
        SIGWINCH,
        SIGIO,
        SIGPOLL,
        SIGPWR,
        SIGSYS = Value
}


/**
*   @author dmilith
*   
*   Case classes which depends on POSIXSignals
*/
import POSIXSignals._
case class SendSignal(val signal: POSIXSignals = SIGINT, @specialized val pid: Int)


/**
*   @author dmilith
*   
*   POSIX trait with basic glibc functions (JNA)
*/
trait POSIX extends Library {
    
    @specialized def chmod(filename: String, @specialized mode: Int)
    @specialized def chown(filename: String, @specialized user: Int, @specialized group: Int)
    @specialized def touch(filename: String)
    @specialized def rename(oldpath: String, newpath: String)
    @specialized def kill(@specialized pid: Int, @specialized signal: Int): Int
    @specialized def symlink(oldpath: String, newpath: String): Int
    @specialized def mkdir(path: String, @specialized mode: Int): Int
    @specialized def execl(comm: String): Int
    @specialized def rmdir(path: String): Int
    @specialized def geteuid: Int
    
}


/**
*   @author dmilith
*   
*   PSTREE - ServeD native library (JNA)
*/
trait PSTREE extends Library {
    
    /**
    *   @author dmilith
    *   
    *   Return list of processes as char*
    *   
    *   Arguments: 
    *       show: 0/1, 1 would show user processes threads
    *       sort: 0/1, 0 would give unsorted list of processes
    *   
    */   
    @specialized def processes(@specialized show: Int, @specialized sort: Int): String
    
}
