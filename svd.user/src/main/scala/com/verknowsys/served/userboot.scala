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
import scala.concurrent.duration._
import akka.actor._
import scala.io.Source
import java.io.File
import java.lang.{System => JSystem}


object userboot extends SvdAkkaSupport with Logging {


    def main(args: Array[String]) {

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

                // set runtime properties
                JSystem.setProperty("org.terracotta.quartz.skipUpdateCheck", "true")
                JSystem.setProperty("user.name", userUID) // required for ServeD
                JSystem.setProperty("user.home", SvdConfig.userHomeDir / userUID) // required for ServeD

                // Get account form remote service
                log.info("Dispatching Account Manager for uid %s", userUID)
                val rb = system.actorOf(Props(new SvdUserBoot(userUID.toInt)).withDispatcher("svd-core-dispatcher"), "SvdUserBoot")

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
