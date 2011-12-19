package com.verknowsys.served


import com.verknowsys.served.utils._
import com.verknowsys.served.managers.LoggingManager
import com.verknowsys.served.managers.SvdAccountManager
import com.verknowsys.served.api._

import akka.actor._
import akka.actor.Actor.{remote, actorOf, registry}



object LocalAccountsManager extends GlobalActorRef(
    remote.actorFor("service:accounts-manager", SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort)
)

object userboot extends Logging {
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

        val accountOpt = (LocalAccountsManager !! GetAccount(userUID)) collect { case Some(account: SvdAccount) => account }
        val portOpt = (LocalAccountsManager !! GetPort) collect { case i: Int => i }

        (for {
            account <- accountOpt
            port <- portOpt
        } yield {
            log.debug("Got account, starting AccountManager for %s at port %d", account, port)
            val am = actorOf(new SvdAccountManager(account)).start

            val loggingManager = actorOf(new LoggingManager(GlobalLogger)).start
            am !! Init
            remote.start(SvdConfig.defaultHost, port) // XXX: hack: both defaultHost and port
            remote.register("service:account-manager", am)
            remote.register("service:logging-manager", loggingManager)

            SvdUtils.addShutdownHook {
                log.info("Shutdown of user svd requested")
                am.stop
            }

            log.info("Spawned UserBoot for UID: %d", userUID)

        }) getOrElse {
            log.error("Account for uid %d does not exist", userUID)
            sys.exit(1)
        }
    }




    def main(args: Array[String]) {
        // handle signals
        SvdUtils.handleSignal("ABRT") { SvdUtils.getAllLiveThreads }
        SvdUtils.handleSignal("USR2") { log.warn("TODO: implement USR2 handling (show svd config values)") }

        log.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)

        args.toList match {
            case userid :: xs =>
                log.info("Spawning user (%d)", userid.toInt)
                run(userid.toInt)
            case _ =>
                log.error("Invalid arguments.")
                log.error("Usage: [jar] [userid]   - ServeD user")
                sys.exit(1)
        }
    }
}
