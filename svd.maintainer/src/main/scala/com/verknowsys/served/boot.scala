package com.verknowsys.served


import akka.actor._
import akka.config.Supervision._
import akka.actor.Actor.{remote, actorOf, registry}
import akka.util.Logging

import com.verknowsys.served.utils.SvdUtils
import com.verknowsys.served.utils.SvdFileEventsManager
import com.verknowsys.served.maintainer.SvdMaintainer
import com.verknowsys.served.maintainer.SvdSystemInfo
import com.verknowsys.served.systemmanager.SvdAccountsManager
import com.verknowsys.served.maintainer.SvdApiSession
import com.verknowsys.served.notifications.SvdNotificationCenter
import com.verknowsys.served.systemmanager.SvdSystemManager

import com.verknowsys.served.utils.signals._



object boot extends Logging {


    def apply(){
        val list = (actorOf[SvdFileEventsManager] ::
                   actorOf[SvdSystemManager] ::
                   actorOf[SvdAccountsManager] :: 
                   actorOf[SvdMaintainer] ::
                   actorOf[SvdSystemInfo] ::
                   // actorOf[SvdNotificationCenter] :: 
                   Nil).map(Supervise(_, Permanent))
        // supervise and autostart
        Supervisor(
          SupervisorConfig(
            OneForOneStrategy(List(classOf[Exception], classOf[RuntimeException], classOf[NullPointerException]), 50, 1000),
            list))


        // 2011-02-01 21:13:24 - dmilith - NOTE: this is default order of starting Managers and Maintainer:
        registry.actorFor[SvdAccountsManager].foreach { _ ! Init }
        registry.actorFor[SvdSystemManager].foreach { _ ! Init }
        registry.actorFor[SvdMaintainer].foreach { _ ! Init }
        
        // ApiServer
        remote.start(SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort)
        remote.registerPerSession("service:api", actorOf[SvdApiSession])
    }
    

    def main(args: Array[String]) {
        
        SvdUtils.checkOrCreateVendorDir
        
        if (SvdUtils.isLinux) {
            log.error("Linux systems aren't supported yet!")
            System.exit(1)
        }
        
        log.debug("Home dir: " + SvdConfig.homePath + SvdConfig.vendorDir)
        log.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)
        
        args foreach { _ match {
            case "--monitor" =>

            case x: Any => 
                log.error("Unknow argument: %s. Exiting", x)
                System.exit(1)
        }}
        
        log.info("Starting %s".format(SvdConfig.served))
        boot()
    }
    
    
}