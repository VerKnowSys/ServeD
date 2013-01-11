/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served


import com.verknowsys.served.utils._
// import com.verknowsys.served.maintainer.SvdApiConnection
// import com.verknowsys.served.notifications.SvdNotificationCenter
import com.verknowsys.served.api._
import java.lang.{System => JSystem}
import akka.actor._


object rootboot extends Logging with SvdUtils with App {



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

    def run {
        println()
        println()
        println("=========================")
        println("===   ServeD - core   ===")
        println("=========================")
        println()
        println()
        log.info(SvdConfig.servedFull)
        log.info(SvdConfig.copyright)

        // set runtime properties
        JSystem.setProperty("org.terracotta.quartz.skipUpdateCheck", "true")

        val rb = system.actorOf(Props(new SvdRootBoot).withDispatcher("svd-core-dispatcher"))
        addShutdownHook {
            log.warn("rootboot shutdown hook will wait for system")
            Thread.sleep(SvdConfig.shutdownTimeout / 2)

            system.stop(rb)
            system.shutdown
        }

    }

    // handle signals
    handleSignal("ABRT") { getAllLiveThreads }
    handleSignal("USR2") { log.warn("TODO: implement USR2 handling (show svd config values)") }

    run
}
