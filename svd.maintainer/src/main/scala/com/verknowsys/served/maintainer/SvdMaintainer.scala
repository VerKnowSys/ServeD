// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

// import com.verknowsys.served.api._
import com.verknowsys.served.boot
import com.verknowsys.served.notifications._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils.{SvdUtils, SvdFileEventsManager}
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.systemmanager.SvdSystemManager
import com.verknowsys.served.systemmanager.ProcessesList

// akka
import akka.actor.Actor
import akka.actor.Actor.actorOf
import akka.actor.Actor.registry
import akka.util.Logging

/**
 *  Main SvdMaintainer loader.
 *
 *  @author dmilith, teamon
 */
class SvdMaintainer extends Actor {
    log.info("Maintainer is loading")
        
    registry.actorFor[SvdSystemManager] foreach { _ ! Init }
    registry.actorFor[SvdNotificationCenter] foreach { _ ! Status("Ready!") }
    
    
    def receive = {
        case Init =>
            registry.actorFor[SvdSystemManager] foreach { _ ! GetAllProcesses }
        
        case ProcessesList(pids) =>
            log.trace("Got pids: %s", pids)
            Thread.sleep(2000)
            registry.actorFor[SvdSystemManager] foreach { _ ! GetAllProcesses }
            
        case x => 
            log.warn("not recognized message %s", x)
    }
}
