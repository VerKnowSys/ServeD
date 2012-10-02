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

object userboot extends Logging {


    SvdUtils.addShutdownHook {
        log.warn("Got termination signal")
        log.info("Shutdown of userboot requested")
        system.shutdown
    }

    def run(userUID: Int){
        log.info("ServeD v" + SvdConfig.version)
        log.info(SvdConfig.copyright)

        println()
        println()
        println("=========================")
        println("===   ServeD - %4s   ===".format(userUID))
        println("=========================")
        println()
        println()

        // Get account form remote service
        log.debug("Getting account for uid %d", userUID)
        val akkaConfig = Source.fromFile(SvdConfig.userHomeDir / "%s".format(userUID) / "akka.conf").getLines.mkString("\n")
        log.trace("Read akka configuration for account: %s", akkaConfig)

        val system = ActorSystem(SvdConfig.served, ConfigFactory.parseString(akkaConfig).getConfig("ServeDremote"))
        // implicit val timeout = Timeout(30 seconds)
        val accountsManager = system.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode

        accountsManager ! Alive(501)

        (accountsManager ? GetAccount(userUID)) onSuccess {

            case Some(account: SvdAccount) =>

                log.debug("Got account, starting AccountManager for %s on account manager port: %d", account, account.accountManagerPort)
                val am = system.actorOf(Props(new SvdAccountManager(account)).withDispatcher("svd-single-dispatcher"))
                // SvdAccountsManager ask SetAccountManager(account.uid, am)
                val loggingManager = system.actorOf(Props(new LoggingManager(GlobalLogger)))
                am ! Init

                // remote.start(SvdConfig.defaultHost, port) // XXX: hack: both defaultHost and port
                // remote.register("service:account-manager", am)
                // remote.register("service:logging-manager", loggingManager)

                // SvdUtils.addShutdownHook {
                //     log.info("Shutdown of user svd requested")
                //     system.shutdown
                //     // am.shutdown
                // }

                log.info("Spawned UserBoot for UID: %d", userUID)

            case None =>
                log.error("No account with uid %d".format(userUID))

        } onFailure {
            case x =>
                log.error("Error occured in userboot with: %s".format(x))
                SvdUtils.throwException[RuntimeException]("Cannot spawn user boot for UID: %s!".format(userUID))
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
        SvdUtils.handleSignal("ABRT") { SvdUtils.getAllLiveThreads }
        SvdUtils.handleSignal("USR2") { log.warn("TODO: implement USR2 handling (show svd config values)") }

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
