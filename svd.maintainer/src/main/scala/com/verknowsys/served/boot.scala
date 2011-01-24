package com.verknowsys.served

import akka.actor._
import akka.config.Supervision._
import akka.actor.Actor.{remote, actorOf, registry}
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
        val list = (actorOf[SvdFileEventsManager] ::
                   actorOf[SvdSystemManager] ::
                   actorOf[SvdMaintainer] ::
                   actorOf[SvdAccountsManager] :: 
                   // actorOf[SvdNotificationCenter] :: 
                   Nil).map(a => Supervise(a, Permanent))
        // supervise and autostart
        Supervisor(
          SupervisorConfig(
            OneForOneStrategy(List(classOf[Exception]), 3, 1000),
            list))
        
        registry.actorFor[SvdMaintainer].foreach { _ ! Init }
        
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