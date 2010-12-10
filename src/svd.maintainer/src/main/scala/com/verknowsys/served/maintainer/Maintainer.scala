// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

import com.verknowsys.served.api._
import com.verknowsys.served.Config
import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.monitor._
import com.verknowsys.served.systemmanager._

import scala.collection.JavaConversions._
import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node
import org.apache.log4j.{Level, Logger}

import com.verknowsys.served.notifications._


/**
 *  @author dmilith
 *
 *  Main Maintainer loader.
 */

object Maintainer extends Actor with Monitored with Utils {
    start

    
    def act {
        loop {
            receive {
                case Init =>
                    logger.info("Maintainer ready")
                    reply(Ready)

                case Quit =>
                    logger.info("Quitting Maintainer")
                    reply(Ready)
                    exit

                // case GetUsers(x) =>
                //     val content = x.map {a => "userName: " + a.userName + ", pass: " + a.pass + ", uid: " + a.uid + ", gid: " + a.gid + ", homeDir: " + a.homeDir + ", shell: " + a.shell + ", information: " + a.information + "\n"}
                //     logger.debug("Content:\n" + content)

                case x: AnyRef =>
                    logger.trace("Command not recognized. Maintainer will ignore signal: " + x.toString)

            }
        }
    }
    
    /**
     * @author teamon
     */
    override def toString = "!Maintainer!"


    /**
    *   @author dmilith  
    *   
    *   ServeD Maintainer Core
    */
    def main(args: Array[String]) {
        
        args foreach { _ match {
            case "--monitor" => 
                Monitor.start
             
            case x => 
                logger.error("Unknow argument: " + x)
                System.exit(1);
        }}

        logger.debug("Mainainer object size: " + sizeof(Maintainer))
        logger.debug("Maintainer home dir: " + Config.homePath + Config.vendorDir)
        logger.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)

        addShutdownHook {
            // SvdSystemManager !? Quit
            AccountsManager !? Quit
            // NotificationCenter !? Quit
            Maintainer !? Quit
        }

        // logger.info("Maintainer is loading")
        // Maintainer !? Init
        
        // logger.info("NotificationCenter is loading")
        // NotificationCenter !? Init
        
        logger.info("AccountManager is loading")
        AccountsManager ! Init
        
        // logger.info("SystemManager is loading")
        // SvdSystemManager !? Init
        
        // logger.info("ApiServerActor is loading")
        // ApiServerActor !? Init
    }


}
