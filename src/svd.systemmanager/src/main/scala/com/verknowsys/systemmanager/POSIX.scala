// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.sun.jna.Library


/**
*   @author dmilith
*   
*   POSIXSignals enum definition
*/
object POSIXSignals extends Enumeration(initial = 1) {
    type POSIXSignals = Value
    val SIGHUP, // 2010-10-05 15:03:27 - dmilith - NOTE: should have value 1
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
case class SendSignal(val signal: POSIXSignals = SIGINT, val pid: Int)


/**
*   @author dmilith
*   
*   POSIX trait with basic glibc functions (JNA)
*/
trait POSIX extends Library {
    
    def chmod(filename: String, mode: Int)
    def chown(filename: String, user: Int, group: Int)
    def touch(filename: String)
    def rename(oldpath: String, newpath: String)
    def kill(pid: Int, signal: Int): Int
    def symlink(oldpath: String, newpath: String): Int
    def mkdir(path: String, mode: Int): Int
    def execl(comm: String): Int
    def rmdir(path: String): Int
    def geteuid: Int
    
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
    def processes(show: Int, sort: Int): String
    
}
