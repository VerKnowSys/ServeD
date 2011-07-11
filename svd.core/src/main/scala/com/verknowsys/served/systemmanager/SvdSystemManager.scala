// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.api._
import com.verknowsys.served.utils.monitor.SvdMonitored
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.Logging
import com.verknowsys.served.utils.GlobalActorRef

import org.hyperic.sigar._
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import akka.actor.Actor
import com.sun.jna.{Native, Library}
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._


object SvdSystemManager extends GlobalActorRef(Actor.registry.actorFor[SvdSystemManager])


/**
*   @author dmilith
*   
*   SvdSystemManager - responsible for System managment and monitoring
*/
class SvdSystemManager extends SvdExceptionHandler {

    log.info("SvdSystemManager is loading")
    

    def receive = {
        case Init =>
            log.debug("SvdSystemManager ready")
            // log.info("Sigar version loaded: %s".format(core.getVersion))
            log.debug("System Resources Availability: [%s]".format(SvdLowLevelSystemAccess))
            log.debug("Current PID: %d. System Information:\n%s".format(SvdLowLevelSystemAccess.getCurrentProcessPid, SvdLowLevelSystemAccess.getProcessInfo(SvdLowLevelSystemAccess.getCurrentProcessPid)))
            log.debug("Network configuration: GW: %s, DOMAIN: %s, HOST: %s, DNS1: %s, DNS2: %s",
                SvdLowLevelSystemAccess.net.getDefaultGateway, SvdLowLevelSystemAccess.net.getDomainName, SvdLowLevelSystemAccess.net.getHostName, SvdLowLevelSystemAccess.net.getPrimaryDns, SvdLowLevelSystemAccess.net.getSecondaryDns
            )
            
        case GetAllProcesses =>
            val psAll = SvdLowLevelSystemAccess.processList(true)
            log.debug("All process IDs: %s".format(psAll.mkString(", ")))
            // self reply ProcessesList(psAll)
        
        case GetNetstat =>
            SvdLowLevelSystemAccess.netstat.stat(SvdLowLevelSystemAccess.core)
            log.warn("Network usage (bytes): IN: %s, OUT: %s".format(SvdLowLevelSystemAccess.netstat.getTcpInboundTotal, SvdLowLevelSystemAccess.netstat.getTcpOutboundTotal))
            // self reply Success
            
        case Quit =>
            log.info("Quitting SvdSystemManager")
            sys.exit(0)
            
        case Chown(what, userId, groupId, recursive) =>
            log.debug("Chown called on location: '%s' with uid: %s, gid: %s (recursively: %s)".format(what, userId, groupId, recursive))
            SvdSystemManagerUtils.chown(what, userId, SvdConfig.defaultUserGroup, recursive)
            
        case Chmod(what, mode, recursive) =>
            log.debug("Chmod called on location: '%s' with mode: %s (recursively: %s)".format(what, mode, recursive))
            SvdSystemManagerUtils.chmod(what, mode, recursive)
        
        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))
            
    }
    
    
    override def toString = "SvdSystemManager"


}
