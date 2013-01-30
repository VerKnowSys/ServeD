/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.managers


import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.Events._
import com.verknowsys.served.utils.signals.SvdPOSIX._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.api.pools._
import com.verknowsys.served.services._
import com.verknowsys.served.api.scheduler._

import akka.actor._
import scala.io.Source
import scala.concurrent._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import org.quartz._
import org.quartz.impl._
import org.quartz.JobKey._
import org.quartz.impl.matchers._


/**
 *  ServeD Accounts Manager
 *
 *  @author dmilith
 */
class SvdAccountsManager extends SvdManager with SvdFileEventsReactor with Logging {

    import Events._


    implicit val timeout = Timeout(SvdConfig.headlessTimeout / 1000 seconds) // cause of standard
    // override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 25, withinTimeRange = 1 minute) {
    //     case _: ArithmeticException      => Resume
    //     case _: NullPointerException     => Restart
    //     case _: IllegalArgumentException => Stop
    //     case _: Exception                => Escalate
    // }
    val scheduler = StdSchedulerFactory.getDefaultScheduler




    override def preStart = {
        super.preStart
        log.info("Starting AccountsManager Quartz Scheduler")
        scheduler.start

        log.info(s"SvdAccountsManager (v${SvdConfig.version}) is loading")
        // launchSystemServices
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
            log.info(s"Becoming aware of alive account: ${account}")
            log.debug(s"Alive accounts: ${account :: accountsAlive}")


        /**
         * @author Daniel (dmilith) Dettlaff
         * @since 0.6
         *
         *  Notify that an account went offline.
         *
         */
        case Admin.Dead(account) =>
            val accountsWithoutThisOne = accountsAlive.filterNot{_.uuid == account.uuid}
            log.info(s"Becoming aware of dead account: ${account}")
            log.debug(s"Alive accounts: ${accountsWithoutThisOne}")
            context.become(
                awareOfUserManagers(accountsWithoutThisOne))
            sender ! ApiSuccess


        case SvdScheduler.StartJob(name, job, trigger) =>
            log.debug("Starting schedule job named: %s for service: %s".format(name, sender))
            scheduler.scheduleJob(job, trigger)


        case SvdScheduler.StopJob(name) =>
            log.debug("Stopping scheduled jobs named: %s for service: %s".format(name, sender))
            for (index <- 0 to SvdConfig.maxSchedulerDefinitions) { // XXX: hacky.. it's better to figure out how to get list of defined jobs from scheduler..
                scheduler.deleteJob(jobKey("%s-%d".format(name, index)))
            }


        case SvdFileEvent(path, flags) =>
            log.trace(s"REACT on file event on path: ${path}. Flags no: ${flags}")
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
                    log.trace(s"Got event: ${x}")
            }


        case ApiSuccess =>
            log.debug("Got success")


        case Notify.Message(x) =>
            log.trace(s"Received Message: ${x}")


        case Terminated(ref) =>
            log.debug(s"SvdAccountsManager received Terminate service for: ${ref}")
            context.unwatch(ref)


        case x: Any =>
            log.warn(s"${this} has received unknown signal: ${x}")
            // sender ! ApiError("Unknown signal %s".format(x))

    }


    def receive = awareOfUserManagers(Nil)


    override def postStop = {
        log.debug("Accounts Manager postStop.")
        log.info("Shutting down AccountsManager Scheduler")
        scheduler.shutdown
        // sendTerminationSignalForAllSuperServices
        super.postStop
    }


    addShutdownHook {
        log.warn("Got termination signal. Unregistering file events")
        // sendTerminationSignalForAllSuperServices
        unregisterFileEvents(self)
        log.info("All done.")
    }

}
