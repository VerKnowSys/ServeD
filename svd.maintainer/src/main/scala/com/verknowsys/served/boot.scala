package com.verknowsys.served

import akka.actor.ActorRef
import akka.actor.Actor.{remote, actorOf}
import akka.util.Logging

import com.verknowsys.served.utils.SvdFileEventsManager
import com.verknowsys.served.maintainer.SvdMaintainer
import com.verknowsys.served.maintainer.SvdAccountsManager
import com.verknowsys.served.maintainer.SvdApiSession
import com.verknowsys.served.notifications.SvdNotificationCenter
import com.verknowsys.served.systemmanager.SvdSystemManager

import com.verknowsys.served.utils.signals._

object boot extends Logging {
    def apply(){
        actorOf[SvdFileEventsManager].start
        actorOf[SvdSystemManager].start
        actorOf[SvdMaintainer].start ! Init
        actorOf[SvdAccountsManager].start
        // actorOf[SvdNotificationCenter].start
        
        // ApiServer
        remote.start(SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort)
        remote.registerPerSession("service:api", actorOf[SvdApiSession])
    }
    
    def main(args: Array[String]) {
        log.debug("SvdConfig home dir: " + SvdConfig.homePath + SvdConfig.vendorDir)
        log.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)
        
        args foreach { _ match {
            case "--monitor" =>

            case x: Any => 
                log.error("Unknow argument: %s. Exiting", x)
                System.exit(1)
        }}
        
        boot()

    }
}