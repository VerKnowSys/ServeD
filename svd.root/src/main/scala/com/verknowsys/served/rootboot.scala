package com.verknowsys.served


import com.verknowsys.served.utils._
import com.verknowsys.served.managers.LoggingManager
import com.verknowsys.served.maintainer.SvdSystemInfo
import com.verknowsys.served.maintainer.SvdApiConnection
import com.verknowsys.served.managers.SvdAccountsManager
import com.verknowsys.served.systemmanager.SvdSystemManager
// import com.verknowsys.served.notifications.SvdNotificationCenter
import com.verknowsys.served.sshd.SSHD
import com.verknowsys.served.api._

import akka.actor._
import akka.config.Supervision._
import akka.actor.Actor.{remote, actorOf, registry}


object rootboot extends Logging {
    def run {
        println()
        println()
        println("=========================")
        println("===   ServeD - core   ===")
        println("=========================")
        println()
        println()

        val sshd = actorOf(new SSHD(SvdConfig.sshPort))

        val list = (
            actorOf[SvdFileEventsManager] ::
            LoggingManager() ::
            SvdSystemManager() ::
            SvdAccountsManager() ::
            actorOf[SvdSystemInfo] ::
            // actorOf[SvdNotificationCenter] ::
            sshd ::
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

        SvdSystemManager ! Init
        SvdAccountsManager ! Init


        // Remote services
        remote.start(SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort)
        remote.registerPerSession("service:api", actorOf[SvdApiConnection])
        remote.register("service:accounts-manager", SvdAccountsManager())
    }

    def main(args: Array[String]) {
        if (SvdUtils.isLinux) {
            log.error("Linux systems aren't supported yet!")
            sys.exit(1)
        }

        // handle signals
        SvdUtils.handleSignal("ABRT") { SvdUtils.getAllLiveThreads }
        SvdUtils.handleSignal("USR2") { log.warn("TODO: implement USR2 handling (show svd config values)") }

        log.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)

        run
    }
}
