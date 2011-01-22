// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

// import com.verknowsys.served.api._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils.{SvdUtils}
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.systemmanager._
// 
// import scala.collection.JavaConversions._
// 
// import com.verknowsys.served.notifications._



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
class SvdMaintainer extends Actor with Logging {
    log.trace("SvdMaintainer is loading")
    
    self.spawnLink[SvdAccountsManager]
    self.spawnLink[SvdSystemManager]
        
    def receive = {
        case x => log.warn("not recognized message %s", x)
    }
}
    
/**
*   @author dmilith  
*   
*   ServeD SvdMaintainer Core
*/
object SvdMaintainer extends Logging {
    def main(args: Array[String]) {
        log.debug("Mainainer object size: " + SvdUtils.sizeof(SvdMaintainer))
        log.debug("SvdMaintainer home dir: " + SvdConfig.homePath + SvdConfig.vendorDir)
        log.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)
        
        SvdUtils.rootCheck
        

        
        // args foreach { _ match {
        //     case "--monitor" => 
        //         SvdMonitor.start
        //      
        //     case x: Any => 
        //         error("Unknow argument: " + x)
        //         System.exit(1);
        // }}

        
        // SvdUtils.addShutdownHook {
        //    \ SvdSystemManager\ ! Quit
        //     SvdAccountsManager ! Quit
        //     SvdNotificationCenter ! Quit
        //     SvdMaintainer ! Quit
        // }

        val maintainer = actorOf[SvdMaintainer]
        maintainer.start
        
        maintainer ! 0 // HACK: akka does not start if no message sent
        
        // info("SvdNotificationCenter is loading")
        // SvdNotificationCenter ! Init
        
        // info("SvdAccountSvdManager is loading")
        // SvdAccountsManager ! Init
        
        val ssm = Actor.registry.actorFor[SvdSystemManager]
        ssm.get ! Init
        
        // log.info("SvdSystemManager is loading")

        
        // info("ApiServer is loading")
        // ApiServer.start
        
        // SvdUtils.getAllLT
    }


}
