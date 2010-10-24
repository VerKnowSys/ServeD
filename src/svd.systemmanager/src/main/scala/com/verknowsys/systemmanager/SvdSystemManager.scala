// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.systemmanager._

import scala.actors.Actor
import com.sun.jna.{Native, Library}



/**
*   @author dmilith
*   
*   SystemManager - responsible for System Managment and Monitoring
*/
object SvdSystemManager extends Actor with Utils {
    
    start
    
    
    def act {
        Native.setProtected(true) // 2010-10-11 23:43:21 - dmilith - set JVM protection (in case of JNA code fail it should only throw an exception)
        Actor.loop {
            receive {
                case Init =>
                    logger.info("SystemManager ready")
                    logger.trace("Process list: %s".format(processList().mkString)) // no args == show user threads and sort output
                    
                case Quit =>
                    logger.info("Quitting SystemManager…")
                
                case SendSignal(signal, pid) =>
                    sendSignalToPid(signal, pid)
                    logger.trace("Send Signal Request, received with signal %s, for pid: %s".format(signal, pid))
                
                case x: AnyRef =>
                    logger.trace("Command not recognized. SystemManager will ignore You: %s".format(x))
                    
            }
        }
    }


    /**
    *   @author dmilith
    *   
    *   This function is a bridge to low level libc functions
    */
    def posixlib = Native.loadLibrary("c", classOf[POSIX]).asInstanceOf[POSIX]


    /**
    *   @author dmilith
    *   
    *   This function is a bridge to low level pstree implementation
    */
    def pstreelib = Native.loadLibrary("pstree", classOf[PSTREE]).asInstanceOf[PSTREE]


    /**
    *   @author dmilith  
    *   
    *   This function will send given signal (first param), to given pid (second param)
    */
    def sendSignalToPid(signal: POSIXSignals.Value, pid: Int) =
        signal match {
            case POSIXSignals.SIGHUP =>
                logger.trace("SigHUP sent to process pid: %s".format(pid))
                
            case POSIXSignals.SIGINT =>
                logger.trace("SigINT sent to process pid: %s".format(pid))
                
            case POSIXSignals.SIGQUIT =>
                logger.trace("SigQUIT sent to process pid: %s".format(pid))
                
            case POSIXSignals.SIGKILL =>
                logger.trace("SigKILL sent to process pid: %s".format(pid))
            
            case x: AnyRef =>
                logger.trace("Command not recognized. sendSignalToPid will ignore You: %s".format(x))
                
        }


    /**
    *   @author dmilith
    *   
    *   Converts processes as String to List of SystemProcess'es.
    *   
    *   Arguments: 
    *       show: Boolean. Default: true. 
    *           If true then it will return user processes with listed every thread of every process.
    *       sort: Boolean. Default: true.
    *           If true then it will return sorted alphabetically list of processes.
    *
    */
    def processList(showThreads: Boolean = true, sort: Boolean = true): List[SystemProcess] = {
        val st = if (showThreads) 1 else 0
        val so = if (sort) 1 else 0
        for(process <- pstreelib.processes(st, so).split("/").toList.tail) // 2010-10-24 01:09:51 - dmilith - NOTE: toList, cause JNA returns Java's "Array" here.
            yield
                new SystemProcess(
                    processName = process.split(",").head,
                    pid = process.split(",").last
                )
                
    }
    
    
    /**
    *   @author dmilith
    *   
    *   Returns System Process count.
    */
    def processCount(showThreads: Boolean = true, sort: Boolean = true) = processList(showThreads, sort).size
    
    
}
