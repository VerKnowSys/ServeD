// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

// import com.verknowsys.served.api._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils.{Utils, SvdFileEventsManager}
// import com.verknowsys.served.utils.signals._
import com.verknowsys.served.systemmanager.SvdSystemManager

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
class Maintainer(skipSSM: Boolean = false) extends Actor with Logging {
    log.trace("Maintainer is loading")
    
    self.spawnLink[FileEventsManager]
    self.spawnLink[AccountsManager]
    
    if(skipSSM) log.warn("Skipped SvdSystemManager spawn")
    else self.spawnLink[SvdSystemManager]
    
    ApiServer.start
    
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
        
        var skip = false
        
        args foreach { _ match {
            case "--skip-ssm" =>
                skip = true
                
            case x: Any => 
                error("Unknow argument: " + x)
                System.exit(1)
        
        }}

         
        if(!skip) Utils.rootCheck // TODO: Move it to SSM
        
        actorOf(new Maintainer(skip)).start ! 0
                
        // Utils.addShutdownHook {
        //     SvdSystemManager ! Quit
        //     AccountsManager ! Quit
        //     NotificationCenter ! Quit
        //     Maintainer ! Quit
        // }

        // val maintainer = actorOf[Maintainer]
        // maintainer.start
        
        // maintainer ! 0 // HACK: akka does not start if no message sent
        
        // info("SvdNotificationCenter is loading")
        // SvdNotificationCenter ! Init
        
        // info("SvdAccountManager is loading")
        // SvdAccountsManager ! Init
        
        // val ssm = Actor.registry.actorFor[SvdSystemManager]
        // ssm.get ! Init
        
        
        // log.info("SvdSystemManager is loading")

        
        // info("ApiServer is loading")
        // ApiServer.start
        
        // SvdUtils.getAllLT
    }


}
