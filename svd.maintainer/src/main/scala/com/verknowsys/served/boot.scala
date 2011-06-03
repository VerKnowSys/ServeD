package com.verknowsys.served


import akka.actor._
import akka.config.Supervision._
import akka.actor.Actor.{remote, actorOf, registry}

import com.verknowsys.served.utils.Logging
import com.verknowsys.served.utils.SvdUtils
import com.verknowsys.served.utils.SvdFileEventsManager
import com.verknowsys.served.utils.LoggingManager
import com.verknowsys.served.maintainer.SvdMaintainer
import com.verknowsys.served.maintainer.SvdSystemInfo
import com.verknowsys.served.maintainer.SvdApiConnection
import com.verknowsys.served.systemmanager.SvdAccountsManager
import com.verknowsys.served.systemmanager.SvdSystemManager
import com.verknowsys.served.notifications.SvdNotificationCenter

import com.verknowsys.served.utils.signals._

import sun.misc.SignalHandler
import sun.misc.Signal


object boot extends Logging {


    def apply() {
        val list = (
            actorOf[SvdFileEventsManager] ::
            actorOf[LoggingManager] ::
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
        registry.actorFor[SvdMaintainer].foreach { _ ! Init }
        registry.actorFor[SvdSystemManager].foreach { _ ! Init }
        registry.actorFor[SvdAccountsManager].foreach { _ ! Init }
        
        // ApiServer
        remote.start(SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort)
        remote.registerPerSession("service:api", actorOf[SvdApiConnection])
    }
    
    
    def handleSignal(name: String, block: => Unit) {
        Signal.handle(new Signal(name), new SignalHandler {
            def handle(sig: Signal) {
                log.debug("Signal called: " + name)
                block
            }
        })
    }
    
    
    def handleTrapsOnSignals {
        // NOTE: signal handling:
        handleSignal("USR1", { SvdUtils.getAllLiveThreads })
        handleSignal("USR2", { log.warn("TODO: implement USR2 handling (show svd config values)") })
        
        handleSignal("INT", { sys.exit })
        handleSignal("QUIT", { sys.exit })
        handleSignal("TERM", { sys.exit })
        handleSignal("HUP", { sys.exit })
    }
    
    
    def main(args: Array[String]) {
        SvdConfig.environment = "production"

        handleTrapsOnSignals
        
        SvdUtils.checkOrCreateVendorDir
        
        if (SvdUtils.isLinux) {
            log.error("Linux systems aren't supported yet!")
            sys.exit(1)
        }
        
        log.debug("Home dir: " + (SvdConfig.homePath + "/" + SvdConfig.vendorDir))
        log.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)
        
        log.info("Starting %s (env: %s)", SvdConfig.served, SvdConfig.environment)
        boot()
    }
    
    
}