// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.monitor.SvdMonitored
import com.verknowsys.served.systemmanager.native._

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
*   SvdSystemManager - responsible for System managment and monitoring
*/
class SvdSystemManager extends Actor with Logging with SvdExceptionHandler {

    import SvdPOSIX._
    
    log.info("SvdSystemManager is loading")
    

    def receive = {
        case Init =>
            val nrs = new SvdSystemResources
            
            log.info("SvdSystemManager ready")
            log.info("System Resources Availability: [%s]".format(nrs))
            log.info("Current PID: %d. System Information:\n%s".format(SvdProcess.getCurrentProcessPid, SvdProcess.getProcessInfo(SvdProcess.getCurrentProcessPid)))
            

            val a = new SvdProcess(command = "dig +trace arka.gdynia.pl", user = "root", stdOut = "/tmp/served_nobody_memcached.log")
            // log.debug("%s, status: %s".format(a, if (a.alive) "RUNNING" else "DEAD"))
            a.kill(SIGINT)
            
            val b = new SvdProcess(command = "df -h", user = "root", useShell = true)
            // log.debug("%s, status: %s".format(b, if (b.alive) "RUNNING" else "DEAD"))
            b.kill(SIGINT)
            
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
            SvdProcess.kill(pid.asInstanceOf[Long], signal.asInstanceOf[SvdPOSIX.Value])
            
            SvdUtils.chown("/tmp/dupa007", user = 666, group = 6666)
            SvdUtils.chown("/tmp/dupa_32745923", user = 666, group = 6666)
            
            // throw new Exception("Dupa zbladła")
            // throw new RuntimeException("Dupa zbladła bardzo")
            // throw new Throwable("Dupa biała jak ściana")
            
            
        case SpawnProcess(cmd) =>
            log.debug("Requested process spawn: %s", cmd)
            val spawn = new SvdProcess(cmd, user = "root", useShell = true) // 2011-01-23 05:27:11 - dmilith - XXX: temporary, that should be user's account name
            log.trace("Spawned: %s", spawn)
            
        case GetAllProcesses =>
            val psAll = SvdProcess.processList(true)
            log.debug("All process IDs: %s".format(psAll.mkString(", ")))
            self reply ProcessesList(psAll)
            
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
