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


// object LocalAccountsManager //extends GlobalActorRef(
//     remote.actorFor("service:accounts-manager", SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort)
// )

object userboot extends Logging with SvdUtils {


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
        val configFile = SvdConfig.userHomeDir / "%d".format(userUID) / "akka.conf"
        val akkaConfigContent = Source.fromFile(configFile).getLines.mkString("\n")
        log.trace("Read akka configuration for account: %s", akkaConfigContent)

        val system = ActorSystem(SvdConfig.served, ConfigFactory.parseString(akkaConfigContent).getConfig("ServeDremote"))
        val accountsManager = system.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode

        (accountsManager ? User.GetAccount(userUID)) onSuccess {

            case Some(account: SvdAccount) =>

                log.debug("Got account, starting AccountManager for %s on account manager port: %d", account, account.accountManagerPort)
                val am = system.actorOf(Props(new SvdAccountManager(account)).withDispatcher("svd-single-dispatcher"), "SvdAccountManager") // NOTE: actor name is significant for remote actors!!
                val loggingManager = system.actorOf(Props(new LoggingManager(GlobalLogger)))
                log.info("Spawned UserBoot for UID: %d", userUID)

            case None =>
                log.error("No account with uid %d".format(userUID))

        } onFailure {
            case x =>
                log.error("Error occured in userboot with: %s".format(x))
                throwException[RuntimeException]("Cannot spawn user boot for UID: %s!".format(userUID))
        }

        addShutdownHook {
            log.warn("Got termination signal")
            log.info("Shutting down userboot")
            system.shutdown // shutting down main account actor manager
            Thread.sleep(SvdConfig.shutdownTimeout)
        }

        // val portOpt = (accountsManager ? GetPort) onSuccess {
        //     case i: Int => i
        // }

        // for {
        //     account <- accountOpt
        //     port <- portOpt
        // } yield {
        // }
        // }) getOrElse {
        //     log.error("Account for uid %d does not exist", userUID)
        //     sys.exit(1)
        // }
    }


    def main(args: Array[String]) {
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
