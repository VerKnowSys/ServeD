// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.monitor.SvdMonitored
import com.verknowsys.served.systemmanager.native._

import org.hyperic.sigar._
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import akka.actor.Actor
import com.sun.jna.{Native, Library}
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import akka.util.Logging

case class ProcessesList(pids: List[Long])


/**
*   @author dmilith
*   
*   SvdSystemManager - responsible for System Managment and SvdMonitoring
*/
class SvdSystemManager extends Actor with Logging {
    log.trace("SvdSystemManager is loading")
    
    private val core = new Sigar
    private val processes = ListBuffer[SvdProcess]()
    
    Native.setProtected(true) // 2010-10-11 23:43:21 - dmilith - set JVM protection (in case of JNA code fail it should only throw an exception)
    
    
    def receive = {
        case Init =>
            val nrs = new SvdSystemResources
            val nsp = new SvdSystemProcess(core.getPid)
            
            log.info("SvdSystemManager ready")
            log.info("System Resources Availability:\n%s".format(nrs))
            log.info("Current PID: %d. System Information:\n%s".format(core.getPid, nsp))
            

            // val a = new SvdProcess(command = "memcached -u nobody", user = "root", outputRedirectDestination = "/tmp/served_nobody_memcached.log")
            // log.debug("%s, status: %s".format(a, if (a.alive) "RUNNING" else "DEAD"))
            
            // val b = new SvdProcess(command = "df -h", user = "root")
            // log.debug("%s, status: %s".format(b, if (b.alive) "RUNNING" else "DEAD"))
            
            
            // val sam = Actor.registry.actorFor[SvdAccountManager]
            // sam.get ! "go!"
            
            // new SvdProcess(command = "df -h", user = "dmilith", useShell = false) // without shell it wont work fine
            
            // new SvdProcess(command = "dff -h", user = "dmilith", outputRedirectDestination = "/tmp/df2")
            
            
            // throw new Exception("DUPA1")
            // throw new Exception("DUPA2")
            // throw new Exception
            
            // log.info("after exceptions")
            // 2011-01-11 00:45:18 - dmilith - NOTE: TODO: here will go call after boot of clean system (no rc)
            // self reply((nrs, nsp))
                    
        // case Command(cmd) =>
            // log.info("Running Native Command: %s".format(cmd))
            // val sysManProcess = new SvdProcess(cmd)
            // val result = sysManProcess !? Run // 2011-01-10 23:53:22 - dmilith - NOTE: WAIT FOR PROCESS until end
            // processes.add(sysManProcess)
            // reply(result)
            
        case Kill(cmd) =>
            log.info("Killing Native Command: %s".format(cmd))
            // 2011-01-18 00:50:31 - dmilith - TODO: implement 'kill'
        
        case GetAllProcesses =>
            val psAll = core.getProcList.toList
            log.debug("All process IDs: %s".format(psAll.mkString(", ")))
            // psAll.foreach {
            //     p =>
            //      log.trace(new SvdSystemProcess(p).toString)
            // }
            self reply ProcessesList(psAll)
            
        case GetRunningProcesses =>
            log.debug("Processes running by ServeD: %s".format(processes.mkString(", ")))
            self reply(processes)
    
            
        case Quit =>
            log.info("Quitting SvdSystemManager")
            exit
        
        // case SendSignal(signal, pid) =>
            // trace("Send Signal Request, received with signal %s, for pid: %s".format(signal, pid))
            // reply(Ready)
            
        case _ =>
        
    }


    /**
    *   @author dmilith  
    *   
    *   This function will send given signal (first param), to given pid (second param)
    */
    def sendSignalToPid(signal: SvdPOSIX.Value, @specialized pid: Int) =
        signal match {
            case SvdPOSIX.SIGHUP =>
                log.trace("SigHUP sent to process pid: %s".format(pid))
                
            case SvdPOSIX.SIGINT =>
                log.trace("SigINT sent to process pid: %s".format(pid))
                
            case SvdPOSIX.SIGQUIT =>
                log.trace("SigQUIT sent to process pid: %s".format(pid))
                
            case SvdPOSIX.SIGKILL =>
                log.trace("SigKILL sent to process pid: %s".format(pid))
            
            case _ =>
            
        }


    /**
    *   @author dmilith
    *   
    *   Converts processes as String to List of SvdSystemProcess'es.
    *   
    *   Arguments: 
    *       show: Boolean. Default: true. 
    *           If true then it will return user processes with listed every thread of every process.
    *       sort: Boolean. Default: true.
    *           If true then it will return sorted alphabetically list of processes.
    *
    */
    // def processList(showThreads: Boolean = true, sort: Boolean = true): List[SvdSystemProcess] = {
        // val st = if (showThreads) 1 else 0
        // val so = if (sort) 1 else 0
        // val sourceList = pstreelib.processes(st, so).split("/").toList.filter { a =>
        //                 val tmp = a.split(",").head
        //                 (tmp != "root" && tmp != "init" && tmp != "launchd" && tmp != "") // 2010-10-24 13:59:33 - dmilith - XXX: hardcoded
        //         }
        //         for (process <- sourceList) // 2010-10-24 01:09:51 - dmilith - NOTE: toList, cause JNA returns Java's "Array" here.
        //             yield
        //                 new SvdSystemProcess(
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
            // SvdKqueue.watch(watchedFile, modified = true, deleted = true, renamed = true) {
            //                 val raf = new RandomAccessFile(watchedFile, "r")
            //                 raf.seek(raf.length - 1024)
            //                 log.info("Changed /var/log/kernel.log. Last 1024 bytes: " + raf.readUTF)
            //             }
        // }
    
    
    
    override def toString = "SvdSystemManager"
    
    
}
