// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.kqueue._
import com.verknowsys.served.utils.monitor.Monitored
import com.verknowsys.served.systemmanager.native._

import org.hyperic.sigar._
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import scala.actors.Actor
import scala.actors.Actor._
import com.sun.jna.{Native, Library}
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._


/**
*   @author dmilith
*   
*   SystemManager - responsible for System Managment and Monitoring
*/
object SvdSystemManager extends CommonActor with Monitored {
    
    private val core = new Sigar
    private val processes = ListBuffer[SvdProcess]()
    
    start
    
    
    def act {
        Native.setProtected(true) // 2010-10-11 23:43:21 - dmilith - set JVM protection (in case of JNA code fail it should only throw an exception)
        loop {
            receive {
                case Init =>
                    val nrs = new SystemResources
                    val nsp = new SystemProcess(core.getPid)
                    
                    info("SystemManager ready")
                    info("System Resources Availability:\n%s".format(nrs))
                    info("Current PID: %d. System Information:\n%s".format(core.getPid, nsp))
                    
                    throw new Exception("DUPA1")
                    throw new Exception("DUPA2")
                    throw new Exception
                    
                    info("after exceptions")
                    // 2011-01-11 00:45:18 - dmilith - NOTE: TODO: here will go call after boot of clean system (no rc)
                    reply((nrs, nsp))
                    
                case Command(cmd) =>
                    info("Running Native Command: %s".format(cmd))
                    val sysManProcess = new SvdProcess(cmd)
                    val result = sysManProcess !? Run // 2011-01-10 23:53:22 - dmilith - NOTE: WAIT FOR PROCESS until end
                    processes.add(sysManProcess)
                    reply(result)
                    
                case Kill(cmd) =>
                    info("Killing Native Command: %s".format(cmd))
                    reply(Ready)
                
                case GetAllProcesses =>
                    val psAll = core.getProcList.toList
                    debug("All process IDs: %s".format(psAll.mkString(", ")))
                    psAll.foreach {
                        p =>
                        	trace(new SystemProcess(p))
                    }
                    reply(Ready)
                    
                case GetRunningProcesses =>
                    debug("Processes running by ServeD: %s".format(processes.mkString(", ")))
                    reply(processes)
            
                    
                case Quit =>
                    info("Quitting SystemManager")
                    reply(Ready)
                    exit
                
                // case SendSignal(signal, pid) =>
                    // trace("Send Signal Request, received with signal %s, for pid: %s".format(signal, pid))
                    // reply(Ready)
                    
                case _ =>
                    messageNotRecognized(_)
                    reply(Ready)
            }
        }
    }


    /**
    *   @author dmilith  
    *   
    *   This function will send given signal (first param), to given pid (second param)
    */
    def sendSignalToPid(signal: POSIX.Value, @specialized pid: Int) =
        signal match {
            case POSIX.SIGHUP =>
                trace("SigHUP sent to process pid: %s".format(pid))
                
            case POSIX.SIGINT =>
                trace("SigINT sent to process pid: %s".format(pid))
                
            case POSIX.SIGQUIT =>
                trace("SigQUIT sent to process pid: %s".format(pid))
                
            case POSIX.SIGKILL =>
                trace("SigKILL sent to process pid: %s".format(pid))
            
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
    // def processList(showThreads: Boolean = true, sort: Boolean = true): List[SystemProcess] = {
        // val st = if (showThreads) 1 else 0
        // val so = if (sort) 1 else 0
        // val sourceList = pstreelib.processes(st, so).split("/").toList.filter { a =>
        //                 val tmp = a.split(",").head
        //                 (tmp != "root" && tmp != "init" && tmp != "launchd" && tmp != "") // 2010-10-24 13:59:33 - dmilith - XXX: hardcoded
        //         }
        //         for (process <- sourceList) // 2010-10-24 01:09:51 - dmilith - NOTE: toList, cause JNA returns Java's "Array" here.
        //             yield
        //                 new SystemProcess(
        //                     name = process.split(",").head,
        //                     pid = process.split(",").last
        //                 )
        // Nil        
    // }
    
    
    /**
    *   @author dmilith
    *   
    *   Returns System Process count.
    */
    // @specialized def processCount(@specialized showThreads: Boolean = true, @specialized sort: Boolean = true) = processList(showThreads, sort).size
    
    
    
    /**
    *   @author dmilith
    *   XXX, TESTING, DIRTY, HACK
    *   
    */
    // def watchLogs = {
            // val watchedFile = "/var/log/kernel.log"
            // Kqueue.watch(watchedFile, modified = true, deleted = true, renamed = true) {
            //                 val raf = new RandomAccessFile(watchedFile, "r")
            //                 raf.seek(raf.length - 1024)
            //                 info("Changed /var/log/kernel.log. Last 1024 bytes: " + raf.readUTF)
            //             }
        // }
    
    
    
    override def toString = "SvdSystemManager"
    
    
}
