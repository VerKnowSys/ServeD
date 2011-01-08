// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.kqueue._
import com.verknowsys.served.utils.monitor.Monitored
import com.verknowsys.served.systemmanager._

import org.hyperic.sigar._
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import scala.actors.Actor
import scala.actors.Actor._
import com.sun.jna.{Native, Library}



/**
*   @author dmilith
*   
*   SystemManager - responsible for System Managment and Monitoring
*/
object SvdSystemManager extends Actor with Monitored with Utils {
    
    start
    
    
    def act {
        Native.setProtected(true) // 2010-10-11 23:43:21 - dmilith - set JVM protection (in case of JNA code fail it should only throw an exception)
        loop {
            receive {
                case Init =>
                    logger.info("SystemManager ready")
                    watchLogs
                    
                    reply(Ready)
                    
                case Quit =>
                    logger.info("Quitting SystemManager")
                    reply(Ready)
                    exit
                
                case SendSignal(signal, pid) =>
                    sendSignalToPid(signal, pid)
                    logger.trace("Send Signal Request, received with signal %s, for pid: %s".format(signal, pid))
                
                case _ => messageNotRecognized(_)
                    
            }
        }
    }


    /**
    *   @author dmilith
    *   
    *   This function is a bridge to low level libc functions
    */
    lazy val posixlib = POSIX.instance


    /**
    *   @author dmilith  
    *   
    *   This function will send given signal (first param), to given pid (second param)
    */
    def sendSignalToPid(signal: POSIXSignals.Value, @specialized pid: Int) =
        signal match {
            case POSIXSignals.SIGHUP =>
                logger.trace("SigHUP sent to process pid: %s".format(pid))
                
            case POSIXSignals.SIGINT =>
                logger.trace("SigINT sent to process pid: %s".format(pid))
                
            case POSIXSignals.SIGQUIT =>
                logger.trace("SigQUIT sent to process pid: %s".format(pid))
                
            case POSIXSignals.SIGKILL =>
                logger.trace("SigKILL sent to process pid: %s".format(pid))
            
            case _ => messageNotRecognized(_)                
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
        // val sourceList = pstreelib.processes(st, so).split("/").toList.filter { a =>
        //                 val tmp = a.split(",").head
        //                 (tmp != "root" && tmp != "init" && tmp != "launchd" && tmp != "") // 2010-10-24 13:59:33 - dmilith - XXX: hardcoded
        //         }
        //         for (process <- sourceList) // 2010-10-24 01:09:51 - dmilith - NOTE: toList, cause JNA returns Java's "Array" here.
        //             yield
        //                 new SystemProcess(
        //                     processName = process.split(",").head,
        //                     pid = process.split(",").last
        //                 )
        Nil        
    }
    
    
    /**
    *   @author dmilith
    *   
    *   Returns System Process count.
    */
    @specialized def processCount(@specialized showThreads: Boolean = true, @specialized sort: Boolean = true) = processList(showThreads, sort).size
    
    
    
    /**
    *   @author dmilith
    *   XXX, TESTING, DIRTY, HACK
    *   
    */
    def watchLogs = {
            // val watchedFile = "/var/log/kernel.log"
            // Kqueue.watch(watchedFile, modified = true, deleted = true, renamed = true) {
            //                 val raf = new RandomAccessFile(watchedFile, "r")
            //                 raf.seek(raf.length - 1024)
            //                 logger.info("Changed /var/log/kernel.log. Last 1024 bytes: " + raf.readUTF)
            //             }
        }
    
    
    
    override def toString = "SvdSystemManager"
    
    
}