package com.verknowsys.served.systemmanager

import org.hyperic.sigar._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._

import akka.actor.Actor
import akka.actor.Actor.actorOf
import akka.actor.Actor.registry
import akka.util.Logging

// import com.verknowsys.served.utils._
// import com.verknowsys.served.utils.signals._
// import com.verknowsys.served.utils.events.SvdFileEvent
// import com.verknowsys.served.systemmanager.native._
// import com.verknowsys.served.systemmanager.managers._
// import com.verknowsys.served.api._

/**
 *  @author dmilith
 *
 *   Gatherer will be spawned for each user account by SvdAccountManager.
 *   The main goal for SvdGatherer is to gather user usage stats and write them to file.
 *
 */
class SvdGatherer(
        val userName: String = "root"
    ) extends Actor with Logging {
    
    
    def userPostfix = userName + "/svd.gather"
    
    
    def gatherFileLocation = 
        if (SvdUtils.isBSD)
            "/home/" + userPostfix
        else
            "/Users/" + userPostfix
    
    
    def receive = {
        case _ =>
            log.trace("gatherFileLocation: %s".format(gatherFileLocation))
            log.debug("Received signal in SvdGatherer")
    }
 
    
}