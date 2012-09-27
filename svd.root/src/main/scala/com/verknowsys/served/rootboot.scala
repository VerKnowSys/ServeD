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
import com.typesafe.config.ConfigFactory

import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import akka.actor._


object rootboot extends Logging {

    // override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 100, withinTimeRange = 1 minute)
    // {
    //         case _: ArithmeticException      =>
    //             log.error("ArithmeticException")
    //         case _: NullPointerException     =>
    //             log.error("NullPointerException")
    //         case _: IllegalArgumentException =>
    //             log.error("IllegalArgumentException")
    //         case _: Exception                =>
    //             log.error("Exception")
    // }

    val system = ActorSystem(SvdConfig.served, ConfigFactory.load.getConfig(SvdConfig.served))


    def run {
        println()
        println()
        println("=========================")
        println("===   ServeD - core   ===")
        println("=========================")
        println()
        println()

        val sshd = system.actorOf(Props(new SSHD(SvdConfig.sshPort)), "SvdSSHD") //, new SSHD(SvdConfig.sshPort))
        val ssm = system.actorOf(Props[SvdSystemManager], "SvdSystemManager")
        val sam = system.actorOf(Props[SvdAccountsManager], "SvdAccountsManager") //"akka://%s@deldagorin:5555/user/SvdAccountsManager".format(SvdConfig.served))

        val list = (
            system.actorOf(Props[SvdFileEventsManager], "SvdFileEventsManager") ::
            // system.actorOf(Props(new LoggingManager())) ::
            ssm ::
            sam ::
            system.actorOf(Props[SvdSystemInfo], "SvdSystemInfo") ::
            // actorOf[SvdNotificationCenter] ::
            sshd ::
            Nil) //.map(Supervise(_, Permanent))

        // supervise and autostart
        // val supervisor = Supervisor(
        //     SupervisorConfig(
        //         OneForOneStrategy(List(classOf[Exception], classOf[RuntimeException], classOf[NullPointerException]), 50, 1000),
        //         list
        //     )
        // )

        SvdUtils.addShutdownHook {
            log.info("Shutdown requested")
            system.shutdown
        }

        ssm ! Init
        sam ! Init


        // Remote services
        // remote.start(SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort)
        // remote.registerPerSession("service:api", actorOf[SvdApiConnection])
        // remote.register("service:accounts-manager", SvdAccountsManager())
    }

    def main(args: Array[String]) {
        log.info(SvdConfig.servedFull)
        log.info(SvdConfig.copyright)

        // handle signals
        SvdUtils.handleSignal("ABRT") { SvdUtils.getAllLiveThreads }
        SvdUtils.handleSignal("USR2") { log.warn("TODO: implement USR2 handling (show svd config values)") }

        log.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)

        run
    }
}
