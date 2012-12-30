/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served


import com.verknowsys.served.utils._
import com.verknowsys.served.managers.SvdAccountManager
import com.verknowsys.served.api._

import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import akka.util.duration._
import akka.actor._
import scala.io.Source
import java.io.File
import java.lang.{System => JSystem}


object userboot extends SvdAkkaSupport with Logging {


    def main(args: Array[String]) {
        // set runtime properties
        JSystem.setProperty("org.terracotta.quartz.skipUpdateCheck", "true")

        // handle signals
        handleSignal("ABRT") { getAllLiveThreads }
        handleSignal("USR2") { log.warn("TODO: implement USR2 handling (show svd config values)") }

        log.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)

        args.toList match {
            case userUID :: xs =>
                // log.info("Spawning user with uid: %d", userid.toInt)
                println()
                println()
                println("=========================")
                println("===   ServeD - %4s   ===".format(userUID))
                println("=========================")
                println()
                println()
                log.info("ServeD v" + SvdConfig.version)
                log.info(SvdConfig.copyright)

                // Get account form remote service
                log.info("Dispatching Account Manager for uid %s", userUID)
                val rb = system.actorOf(Props(new SvdUserBoot(userUID.toInt)).withDispatcher("svd-core-dispatcher"))

                addShutdownHook {
                    log.warn("userboot Shutdown Hook invoked")
                    Thread.sleep(SvdConfig.shutdownTimeout)
                    system.stop(rb)
                    system.shutdown // shutting down main account actor manager
                }

            case _ =>
                log.error("Invalid arguments.")
                log.error("Usage: [jar] [userid]   - ServeD user UID")
                sys.exit(1)
        }
    }
}
