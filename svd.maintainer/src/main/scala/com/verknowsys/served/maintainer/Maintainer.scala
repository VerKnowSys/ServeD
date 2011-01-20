// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

// import com.verknowsys.served.api._
import com.verknowsys.served.Config
import com.verknowsys.served.utils.{Utils}
// import com.verknowsys.served.utils.signals._
// import com.verknowsys.served.systemmanager._
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
 *  Main Maintainer loader.
 *
 *  @author dmilith, teamon
 */
class Maintainer extends Actor with Logging {
    log.trace("Maintainer is loading")
    
    self.spawnLink[AccountsManager]
    
    def receive = {
        case _ => println("not recognized message")
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
        
        // args foreach { _ match {
        //     case "--monitor" => 
        //         Monitor.start
        //      
        //     case x: Any => 
        //         error("Unknow argument: " + x)
        //         System.exit(1);
        // }}

        
        // Utils.addShutdownHook {
        //     SvdSystemManager ! Quit
        //     AccountsManager ! Quit
        //     NotificationCenter ! Quit
        //     Maintainer ! Quit
        // }

        val maintainer = actorOf[Maintainer]
        maintainer.start
        
        println(registry.actors.toList)
        Thread.sleep(2000)
        println(registry.actors.toList)
        
        // info("NotificationCenter is loading")
        // NotificationCenter ! Init
        
        // info("AccountManager is loading")
        // AccountsManager ! Init
        
        // info("SystemManager is loading")
        // SvdSystemManager ! Init
        
        // info("ApiServer is loading")
        // ApiServer.start
        
        // Utils.getAllLT
    }


}
