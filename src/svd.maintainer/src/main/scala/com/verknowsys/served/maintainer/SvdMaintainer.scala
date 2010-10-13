// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

import com.verknowsys.served.api._
import com.verknowsys.served.Config
import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.signals.{ProcessMessages, MainLoop, Quit, Init}
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

object ApiServerActor extends Actor with Utils {
    final val port = 5555 // XXX: Hardcoded port number

    RemoteActor.classLoader = getClass().getClassLoader()

    start
    
    def act {
        alive(port)
        register('ServeD, self)
        
        Actor.loop {
            receive {
                case Git.CreateRepository(name) => 
                    logger.trace("Created git repository: " + name)
                    sender ! Git.RepositoryExistsError
                
                case Git.RemoveRepository(name) =>
                    logger.trace("Removed git repository: " + name)
                    sender ! Success
                    
                case Git.ListRepositories =>
                    logger.trace("List repositories")
                    sender ! Git.Repositories(List(
                        Git.Repository("first"),
                        Git.Repository("second")
                    ))
            }
        }
    }
}

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
            NotificationCenter ! Quit
            SvdMaintainer ! Quit
        }

        logger.info("Maintainer is loading…")
        SvdMaintainer ! Init
        
        logger.info("NotificationCenter is loading…")
        NotificationCenter ! Init
        
        logger.info("AccountManager is loading…")
        SvdAccountManager ! Init
        
        logger.info("SystemManager is loading…")
        SvdSystemManager ! Init
        
        logger.info("ApiServerActor is loading…")
        ApiServerActor ! Init
    }


}
