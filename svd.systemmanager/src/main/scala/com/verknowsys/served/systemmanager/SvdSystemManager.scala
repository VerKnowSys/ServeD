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
    import SvdPOSIX._
    
    log.info("SvdSystemManager is loading")
    
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
            

            val a = new SvdProcess(command = "memcached -u nobody", user = "root", outputRedirectDestination = "/tmp/served_nobody_memcached.log")
            log.debug("%s, status: %s".format(a, if (a.alive) "RUNNING" else "DEAD"))
            
            val b = new SvdProcess(command = "df -h", user = "root")
            log.debug("%s, status: %s".format(b, if (b.alive) "RUNNING" else "DEAD"))
            
            
            // val sam = Actor.registry.actorFor[SvdAccountManager]
            // sam.get ! "go!"
            
            // new SvdProcess(command = "df -h", user = "dmilith", useShell = false) // without shell it wont work fine
            
            // new SvdProcess(command = "dff -h", user = "dmilith", outputRedirectDestination = "/tmp/df2")
            
            
            // throw new Exception("Zamierzony EXCEPTION!")
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
            
        case Kill(pid, signal) => // 2011-01-23 04:13:34 - dmilith - NOTE: send standard SIGINT signal to app with some pid
            log.info("Kill request for native application with Pid: %s. Sending signal: %s", pid, signal)
            new SvdProcess("kill -%s %s".format(signal, pid), user = "root")
            
        case SpawnProcess(cmd) =>
            log.debug("Requested process spawn: %s", cmd)
            val spawn = new SvdProcess(cmd, user = "root") // 2011-01-23 05:27:11 - dmilith - XXX: temporary, that should be user's account name
            log.trace("Spawned: %s", spawn)
            
        case GetAllProcesses =>
            val psAll = core.getProcList.toList
            log.debug("All process IDs: %s".format(psAll.mkString(", ")))
            self reply ProcessesList(psAll)
            
        case GetRunningProcesses =>
            log.debug("Processes running by ServeD: %s".format(processes.mkString(", ")))
            self reply(processes)
                
        case Quit =>
            log.info("Quitting SvdSystemManager")
            exit
        
        case _ =>
        
    }


    /**
    *   @author dmilith  
    *   
    *   This function will send given signal (first param), to given pid (second param)
    */
    def sendSignalToPid(signal: SvdPOSIX.Value, pid: Int) =
        signal match {
            case SIGHUP =>
                log.trace("SigHUP sent to process pid: %s".format(pid))
                
            case SIGINT =>
                log.trace("SigINT sent to process pid: %s".format(pid))
                
            case SIGQUIT =>
                log.trace("SigQUIT sent to process pid: %s".format(pid))
                
            case SIGKILL =>
                log.trace("SigKILL sent to process pid: %s".format(pid))
            
            case _ =>
            
        }
    
    
    override def toString = "SvdSystemManager"
    
    
}


/**
*   @author dmilith
*
*   Static access for some functions
*/
object SvdSystemManager {


    // 2011-01-23 04:08:09 - dmilith - XXX: FIXME: TODO: think twice dmilith, refactor this shit cause currently it's a bit messy right? ;f
    // /**
    // *   @author dmilith
    // *   
    // *   Converts processes as String to List of SvdSystemProcess'es.
    // *   
    // *   Arguments: 
    // *       sort: Boolean. Default: false.
    // *           If true then it will return sorted alphabetically list of processes.
    // *
    // */
    // def processList(sort: Boolean = false): List[SvdSystemProcess] = {
    //     val sourceList = (new SvdSystemManager).getProcList.toList.filter {
    //         a =>
    //             val tmp = a.split(",").head
    //             (tmp != "root" && tmp != "init" && tmp != "launchd" && tmp != "") // 2010-10-24 13:59:33 - dmilith - XXX: hardcoded
    //     }
    //     log.debug("PLIST: %s", sourceList)
    //     for (process <- sourceList) // 2010-10-24 01:09:51 - dmilith - NOTE: toList, cause JNA returns Java's "Array" here.
    //         yield
    //             new SvdSystemProcess()
    //     Nil        
    // }
    // 
    // 
    // /**
    // *   @author dmilith
    // *   
    // *   Returns System Process count.
    // */
    // def processCount(sort: Boolean = true) = processList(sort).size

    
}

