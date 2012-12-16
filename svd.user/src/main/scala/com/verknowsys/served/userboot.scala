package com.verknowsys.served


import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.managers.LoggingManager
import com.verknowsys.served.managers.SvdAccountManager
import com.verknowsys.served.api._

import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import akka.actor._
import scala.io.Source
import java.io.File
import java.lang.{System => JSystem}


object userboot extends SvdAkkaSupport with Logging {


    def run(userUID: Int){

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
        log.debug("Getting account for uid %d", userUID)

        val configFile = SvdConfig.userHomeDir / "%d".format(userUID) / SvdConfig.defaultAkkaConfName

        if (!new File(configFile).exists) {
            log.info("Spawning headless".format(userUID))
            val account = SvdAccount(uid = userUID, userName = "headless %s".format(userUID))
            createAkkaUserConfIfNotExistant(userUID, userUID + 1026)
        }

        val akkaConfigContent = Source.fromFile(configFile).getLines.mkString("\n")
        log.trace("Read akka configuration for account: %s", akkaConfigContent)
        val system = try {
            ActorSystem(SvdConfig.served, ConfigFactory.parseString(akkaConfigContent).getConfig("ServeDremote"))
        } catch {
            case _ =>
                ActorSystem(SvdConfig.served, ConfigFactory.parseString(akkaConfigContent).getConfig("ServeDheadless"))
        }
        val accountsManager = system.actorFor("akka://%s@%s:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort))

        implicit val timeout = Timeout(SvdConfig.headlessTimeout / 1000 seconds) // cause of standard of milisecond value in SvdConfig
        (accountsManager ? User.GetAccount(userUID)) onSuccess {

            case Some(account: SvdAccount) =>

                log.debug("Got account, starting AccountManager for %s on account manager port: %d", account, account.accountManagerPort)
                val am = system.actorOf(Props(new SvdAccountManager(account)).withDispatcher("svd-single-dispatcher"), "SvdAccountManager") // NOTE: actor name is significant for remote actors!!
                // val loggingManager = system.actorOf(Props(new LoggingManager(GlobalLogger)))
                log.info("Spawned UserBoot for UID: %d", userUID)

            case None =>
                // log.error("No account with uid %d".format(userUID))

                // XXX: "let anybody in"
                val account = SvdAccount(userName = "anybody", uid = userUID)
                val am = system.actorOf(Props(new SvdAccountManager(account)).withDispatcher("svd-single-dispatcher"), "SvdAccountManager") // NOTE: actor name is significant for remote actors!!
                // val loggingManager = system.actorOf(Props(new LoggingManager(GlobalLogger)))
                log.info("Spawned UserBoot for UID: %d", userUID)


        } onFailure {
            case x =>
                log.debug("Couldn't connect to SvdROOT with UID: %s".format(userUID))

                log.info("Launching headless mode for UID: %d".format(userUID))
                val account = SvdAccount(uid = userUID, userName = "headless %s".format(userUID))

                // launching headless mode
                val am = system.actorOf(Props(new SvdAccountManager(account, headless = true)).withDispatcher("svd-single-dispatcher"), "SvdAccountManager")
                log.info("Spawned Headless UserBoot for UID: %d", userUID)

        }

        addShutdownHook {
            log.warn("userboot Shutdown Hook invoked")
            Thread.sleep(SvdConfig.shutdownTimeout)
            system.shutdown // shutting down main account actor manager
        }
    }


    def main(args: Array[String]) {
        // set runtime properties
        JSystem.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");

        // handle signals
        handleSignal("ABRT") { getAllLiveThreads }
        handleSignal("USR2") { log.warn("TODO: implement USR2 handling (show svd config values)") }

        log.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)

        args.toList match {
            case userid :: xs =>
                log.info("Spawning user (%d)", userid.toInt)
                run(userid.toInt) //, accountsManager, system)

            case _ =>
                log.error("Invalid arguments.")
                log.error("Usage: [jar] [userid]   - ServeD user")
                sys.exit(1)
        }
    }
}
