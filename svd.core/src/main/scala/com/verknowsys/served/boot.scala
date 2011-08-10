package com.verknowsys.served


import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.managers.LoggingManager
import com.verknowsys.served.maintainer.SvdSystemInfo
import com.verknowsys.served.maintainer.SvdApiConnection
import com.verknowsys.served.systemmanager.SvdAccountsManager
import com.verknowsys.served.systemmanager.SvdSystemManager
import com.verknowsys.served.notifications.SvdNotificationCenter
import com.verknowsys.served.systemmanager.managers.SvdAccountManager
import com.verknowsys.served.api._

import akka.actor._
import akka.config.Supervision._
import akka.actor.Actor.{remote, actorOf, registry}
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
        
        val accountsManager = actorOf[SvdAccountsManager]
        val systemManager = actorOf[SvdSystemManager]
        val loggingManager = actorOf(new LoggingManager(GlobalLogger))

        val list = (
            actorOf[SvdFileEventsManager] ::
            loggingManager ::
            systemManager ::
            accountsManager :: 
            actorOf[SvdSystemInfo] ::
            // actorOf[SvdNotificationCenter] :: 
            Nil).map(Supervise(_, Permanent))

        // supervise and autostart
        val supervisor = Supervisor(
            SupervisorConfig(
                OneForOneStrategy(List(classOf[Exception], classOf[RuntimeException], classOf[NullPointerException]), 50, 1000),
                list
            )
        )
        
        SvdUtils.addShutdownHook {
            log.info("Shutdown requested")
            supervisor.shutdown
        }

        systemManager ! Init
        accountsManager ! Init

        // Remote services
        remote.start(SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort)
        remote.registerPerSession("service:api", actorOf[SvdApiConnection])
        remote.register("service:accounts-manager", accountsManager)
    }
    
    
    def user(userUID: Int){
        println()
        println()
        println("=========================")
        println("===   ServeD - %4s   ===".format(userUID))
        println("=========================")
        println()
        println()
        
        // Get account form remote service
        
        log.debug("Getting account for uid %d", userUID)
        val remoteAccountsManager = remote.actorFor("service:accounts-manager", SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort)
        (remoteAccountsManager !! GetAccount(userUID)) collect {
            case Some(account: SvdAccount) => account
        } match {
            case Some(account) =>
                log.debug("Got account, starting AccountManager for %s", account)
                val am = actorOf(new SvdAccountManager(account)).start
                
                val loggingManager = actorOf(new LoggingManager(GlobalLogger)).start
                am !! Init
                remote.start(SvdConfig.defaultHost, account.servicePort)
                remote.register("service:account-manager", am)
                remote.register("service:logging-manager", loggingManager)

                SvdUtils.addShutdownHook {
                    log.info("Shutdown of user svd requested")
                    am.stop
                }

                log.info("Spawned UserBoot for UID: %d", userUID)
                
            case None =>
                log.error("Account for uid %d does not exist", userUID)
                sys.exit(1)
        }
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
