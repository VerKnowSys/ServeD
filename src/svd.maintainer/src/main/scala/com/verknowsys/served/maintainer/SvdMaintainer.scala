// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer


import com.verknowsys.served.Config
import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.signals.{ProcessMessages, MainLoop, Quit, Init}
import com.verknowsys.served.systemmanager._

import scala.collection.JavaConversions._
import actors.Actor
import org.apache.log4j.{Level, Logger}


/**
 * @author dmilith
 *
 * Main Maintainer loader.
 *
 */

object SvdMaintainer extends Actor with Utils {
    
    start

    
    def act {
        Actor.loop {
            receive {
                case Message(x) =>
                    logger.trace("Received message: " + x)

                case Init =>
                    logger.info("Maintainer ready")

                case Quit =>
                    logger.info("Quitting Maintainer…")

                case GetUsers(x) =>
                    val content = x.map {a => "userName: " + a.userName + ", pass: " + a.pass + ", uid: " + a.uid + ", gid: " + a.gid + ", homeDir: " + a.homeDir + ", shell: " + a.shell + ", information: " + a.information + "\n"}
                    logger.debug("Content:\n" + content)

                case x: AnyRef =>
                    logger.trace("Command not recognized. Maintainer will ignore signal: " + x.toString)

            }
        }
    }


    /**
    *   @author dmilith  
    *   
    *   ServeD Maintainer Core
    */
    def main(args: Array[String]) {

        logger.debug("Mainainer object size: " + sizeof(SvdMaintainer))
        logger.debug("Maintainer home dir: " + Config.homePath + Config.vendorDir)
        logger.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)

        addShutdownHook {
            SvdSystemManager ! Quit
            SvdAccountManager ! Quit
            SvdMaintainer ! Quit
        }

        logger.info("Maintainer is loading…")
        SvdMaintainer ! Init
        
        logger.info("AccountManager is loading…")
        SvdAccountManager ! Init
        
        logger.info("SystemManager is loading…")
        SvdSystemManager ! Init

    }


}
