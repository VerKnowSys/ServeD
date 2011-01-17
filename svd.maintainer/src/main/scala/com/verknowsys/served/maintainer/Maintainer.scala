// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

import com.verknowsys.served.api._
import com.verknowsys.served.Config
import com.verknowsys.served.utils.{CommonActor, Utils}
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

object Maintainer extends CommonActor with Monitored {
    start

    
    def act {
        loop {
            receive {
                case Init =>
                    info("Maintainer ready")

                case Quit =>
                    info("Quitting Maintainer")
                    exit

                // case GetUsers(x) =>
                //     val content = x.map {a => "userName: " + a.userName + ", pass: " + a.pass + ", uid: " + a.uid + ", gid: " + a.gid + ", homeDir: " + a.homeDir + ", shell: " + a.shell + ", information: " + a.information + "\n"}
                //     debug("Content:\n" + content)

                case _ => messageNotRecognized(_)

            }
        }
    }
    
    /**
     * @author teamon
     */
    override def toString = "Maintainer"


    /**
    *   @author dmilith  
    *   
    *   ServeD Maintainer Core
    */
    def main(args: Array[String]) {
        debug("Mainainer object size: " + Utils.sizeof(Maintainer))
        debug("Maintainer home dir: " + Config.homePath + Config.vendorDir)
        debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)
        
        args foreach { _ match {
            case "--monitor" => 
                Monitor.start
             
            case x: Any => 
                error("Unknow argument: " + x)
                System.exit(1);
        }}

        
        Utils.addShutdownHook {
            SvdSystemManager ! Quit
            AccountsManager ! Quit
            NotificationCenter ! Quit
            Maintainer ! Quit
        }

        info("Maintainer is loading")
        Maintainer ! Init
        
        // info("NotificationCenter is loading")
        // NotificationCenter ! Init
        
        info("AccountManager is loading")
        AccountsManager ! Init
        
        // info("SystemManager is loading")
        // SvdSystemManager ! Init
        
        info("ApiServer is loading")
        ApiServer ! Init
        
        // Utils.getAllLT
    }


}
