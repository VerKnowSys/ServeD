package com.verknowsys.served


import akka.actor._
import akka.config.Supervision._
import akka.actor.Actor.{remote, actorOf, registry}

import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.managers.LoggingManager
import com.verknowsys.served.maintainer.SvdSystemInfo
import com.verknowsys.served.maintainer.SvdApiConnection
import com.verknowsys.served.systemmanager.SvdAccountsManager
import com.verknowsys.served.systemmanager.SvdSystemManager
import com.verknowsys.served.notifications.SvdNotificationCenter
import com.verknowsys.served.systemmanager.managers.SvdAccountManager
import com.verknowsys.served.systemmanager.native.SvdAccount

import com.verknowsys.served.api._

import sun.misc.SignalHandler
import sun.misc.Signal

object boot extends Logging {

    def svd() {
        println()
        println()
        println("=========================")
        println("===   ServeD - core   ===")
        println("=========================")
        println()
        println()
        
        val list = (
            actorOf[SvdFileEventsManager] ::
            actorOf[LoggingManager] ::
            actorOf[SvdSystemManager] ::
            actorOf[SvdAccountsManager] :: 
            actorOf[SvdSystemInfo] ::
            // actorOf[SvdNotificationCenter] :: 
            Nil).map(Supervise(_, Permanent))
        // supervise and autostart
        Supervisor(
          SupervisorConfig(
            OneForOneStrategy(List(classOf[Exception], classOf[RuntimeException], classOf[NullPointerException]), 50, 1000),
            list))


        registry.actorFor[SvdSystemManager].foreach { _ ! Init }
        registry.actorFor[SvdAccountsManager].foreach { _ ! Init }
        
        // ApiServer
        remote.start(SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort)
        remote.registerPerSession("service:api", actorOf[SvdApiConnection])
    }
    
    def user(userUID: Int){
        println()
        println()
        println("=========================")
        println("===   ServeD - %4s   ===".format(userUID))
        println("=========================")
        println()
        println()
        
        val am = actorOf(new SvdAccountManager(SvdAccount(
            uid = userUID,
            homeDir = "/Users" / userUID.toString
        )))
        remote.start("localhost", 8000)
        remote.register("service:account-manager", am)
        am ! Init
        log.info("Spawned UserBoot for UID: %s".format(userUID))
    }
    
    
    def handleSignal(name: String)(block: => Unit) {
        Signal.handle(new Signal(name), new SignalHandler {
            def handle(sig: Signal) {
                log.warn("Signal called: " + name)
                block
            }
        })
    }
    
    
    def main(args: Array[String]) {
        if (SvdUtils.isLinux) {
            log.error("Linux systems aren't supported yet!")
            sys.exit(1)
        }

        // handle signals
        handleSignal("ABRT") { SvdUtils.getAllLiveThreads }
        handleSignal("USR2") { log.warn("TODO: implement USR2 handling (show svd config values)") }
        
        log.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)
        
        args.toList match {
            case "user" :: userid :: xs =>
                log.info("Spawning user (%d)", userid.toInt)
                user(userid.toInt)
            case "svd" :: xs =>
                log.info("Spawning svd")
                svd()
            case _ =>
                log.error("Invalid arguments.")
                log.error("Usage: [jar] svd             - ServeD root")
                log.error("       [jar] user [userid]   - ServeD user")
                sys.exit(1)
        }
    }
}
