package com.verknowsys.served.systemmanager

import com.sun.jna.{Native, Library}


/**
 * Class which describe any system process
 * 
 * @author dmilith
 */
class SystemProcess(
    val processName: String = "root",
    val pid: String = "0"
) {
    override def toString = "PNAME: %s, PID: %s. ".format(processName, pid)
}

/**
 *   POSIXSignals enum definition
 * 
 * @author dmilith
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
 * Case classes which depends on POSIXSignals
 * 
 * @author dmilith
 */
import POSIXSignals._
case class SendSignal(val signal: POSIXSignals = SIGINT, @specialized val pid: Int)


/**
 * POSIX trait with basic glibc functions (JNA)
 * 
 * @author dmilith
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

object POSIX {
    lazy val instance = Native.loadLibrary("c", classOf[POSIX]).asInstanceOf[POSIX]
}


/**
 * PSTREE - ServeD native library (JNA)
 * 
 * @author dmilith
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
