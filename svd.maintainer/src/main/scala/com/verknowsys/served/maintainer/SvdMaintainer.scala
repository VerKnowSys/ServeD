// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer


// import com.verknowsys.served.api._
import com.verknowsys.served.boot
import com.verknowsys.served.notifications._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils.{SvdUtils, SvdFileEventsManager, SvdExceptionHandler}
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.systemmanager.SvdSystemManager
import com.verknowsys.served.systemmanager.ProcessesList
import com.verknowsys.served.utils.Logging


// akka
import akka.actor.Actor
import akka.actor.Actor.actorOf
import akka.actor.Actor.registry


/**
 *  Main SvdMaintainer loader.
 *
 *  @author dmilith, teamon
 */
class SvdMaintainer extends Actor with SvdExceptionHandler {

    log.info("Maintainer is loading")
        
    registry.actorFor[SvdSystemManager] foreach { _ ! Init }
    registry.actorFor[SvdNotificationCenter] foreach { _ ! Status("Ready!") }
    
    SvdUtils.addShutdownHook {
        log.info("Performing shutdown, after interruption request..")
        sys.exit(0)
    }
    
    def receive = {
        case Init =>
            registry.actorFor[SvdSystemManager] foreach { _ ! GetAllProcesses }
        
        case ProcessesList(pids) =>
            import SvdPOSIX._
            log.trace("Got pids: %s", pids)
            Thread.sleep(SvdConfig.sleepDefaultPause)
            
            // 2011-01-23 05:29:52 - dmilith - NOTE: temporary code:
            // registry.actorFor[SvdSystemManager] foreach { _ ! GetAllProcesses }
            // registry.actorFor[SvdSystemManager] foreach { _ ! SpawnProcess("echo 'dupa'") }
            // registry.actorFor[SvdSystemManager] foreach { _ ! Kill(435343, SIGINT) }
            // 2011-01-23 05:29:52 - dmilith - NOTE: EOF temporary code.

        case x => 
            log.warn("not recognized message %s", x)
    }
    
    
}
