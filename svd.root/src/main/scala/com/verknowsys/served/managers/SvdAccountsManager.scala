/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.managers


import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.api._
import com.verknowsys.served.utils.Events._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.signals.SvdPOSIX._
import com.verknowsys.served.systemmanager.managers._

import com.verknowsys.served.api.pools._
import com.verknowsys.served.services._
import akka.actor._
import scala.io.Source

import scala.concurrent._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

/**
 *  ServeD Accounts Manager
 *
 *  @author dmilith
 */
class SvdAccountsManager extends SvdManager with SvdFileEventsReactor with Logging {

    import Events._


    val systemServices = "Redis" :: Nil // XXX: hardcoded system services



    override def preStart = {
        super.preStart
        log.info(s"SvdAccountsManager (v${SvdConfig.version}) is loading")
        launchSystemServices
    }


    def launchSystemServices = {
        systemServices.map{
            service =>
                val serv = context.actorOf(Props(new SvdSuperService(service)), "SuperService-%s".format(service))
                log.info(s"Launching SuperService: ${serv}")
                context.watch(serv)
        }
    }


    def sendTerminationSignalForAllSuperServices = {
        systemServices.map{
            service =>
                val serv = context.actorFor(s"SuperService-${service}")
                log.info(s"Stopping SuperService: ${serv}")
                context.stop(serv)
        }
    }


    def awareOfUserManagers(accountsAlive: List[SvdAccount]): Receive = {

        /**
         * @author Daniel (dmilith) Dettlaff
         * @since 0.4
         *
         *  Gets first free port available from server.
         *
         */
        case System.GetPort =>
            sender ! SvdAccountUtils.randomFreePort


        /**
         * @author Daniel (dmilith) Dettlaff
         * @since 0.6
         *
         *  Notify that new account maanger is alive and connected to network.
         *
         */
        case Admin.Alive(account) =>
            context.become(
                awareOfUserManagers(account :: accountsAlive))
            log.info("Becoming aware of alive account: %s", account)
            log.debug("Alive accounts: %s".format(account :: accountsAlive))


        /**
         * @author Daniel (dmilith) Dettlaff
         * @since 0.6
         *
         *  Notify that an account went offline.
         *
         */
        case Admin.Dead(account) =>
            val accountsWithoutThisOne = accountsAlive.filterNot{_.uuid == account.uuid}
            context.become(
                awareOfUserManagers(accountsWithoutThisOne))
            sender ! ApiSuccess
            log.info("Becoming aware of dead account: %s", account)
            log.debug("Alive accounts: %s".format(accountsWithoutThisOne))


        case SvdFileEvent(path, flags) =>
            log.trace("REACT on file event on path: %s. Flags no: %s".format(path, flags))
            flags match {
                case Modified =>
                    log.trace("File event type: Modified")
                case Deleted =>
                    log.trace("File event type: Deleted")
                case Renamed =>
                    log.trace("File event type: Renamed")
                case AttributesChanged =>
                    log.trace("File event type: AttributesChanged")
                case Revoked =>
                    log.trace("File event type: Revoked")
                case x =>
                    log.trace("Got event: %s", x)
            }


        case ApiSuccess =>
            log.debug("Got success")


        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))
            // sender ! Error("Unknown signal %s".format(x))

    }


    def receive = awareOfUserManagers(Nil)


    override def postStop = {
        log.debug("Accounts Manager postStop.")
        sendTerminationSignalForAllSuperServices
        super.postStop
    }


    addShutdownHook {
        log.warn("Got termination signal. Unregistering file events")
        sendTerminationSignalForAllSuperServices
        unregisterFileEvents(self)
        log.info("All done.")
    }

}
