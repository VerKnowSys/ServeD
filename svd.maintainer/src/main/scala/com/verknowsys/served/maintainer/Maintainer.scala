// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

// import com.verknowsys.served.api._
import com.verknowsys.served.Config
import com.verknowsys.served.utils.{Utils, FileEventsManager}
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.systemmanager.SvdSystemManager



// akka
import akka.actor.Actor
import akka.actor.Actor.actorOf
import akka.actor.Actor.registry
import akka.util.Logging

/**
 *  Main Maintainer loader.
 *
 *  @author dmilith, teamon
 */
class Maintainer(skipSSM: Boolean = false) extends Actor with Logging {
    log.trace("Maintainer is loading")
    
    self.spawnLink[FileEventsManager]
    self.spawnLink[AccountsManager]
    
    if(skipSSM) log.warn("Skipped SvdSystemManager spawn")
    else self.spawnLink[SvdSystemManager]
    
    def receive = {
        case x => log.warn("not recognized message %s", x)
    }
}
    
/**
*   @author dmilith  
*   
*   ServeD Maintainer Core
*/
object Maintainer extends Logging {
    def main(args: Array[String]) {
        log.debug("Mainainer object size: " + Utils.sizeof(Maintainer))
        log.debug("Maintainer home dir: " + Config.homePath + Config.vendorDir)
        log.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)
        
        
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
        
        // info("NotificationCenter is loading")
        // NotificationCenter ! Init
        
        // info("AccountManager is loading")
        // AccountsManager ! Init
        
        // val ssm = Actor.registry.actorFor[SvdSystemManager]
        // ssm.get ! Init
        
        // log.info("SystemManager is loading")

        
        // info("ApiServer is loading")
        // ApiServer.start
        
        // Utils.getAllLT
    }


}
