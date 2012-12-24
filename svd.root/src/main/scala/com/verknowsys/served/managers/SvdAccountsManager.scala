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

import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._

/**
 *  @author dmilith
 *
 *  ServeD Accounts Manager
 *
 */
class SvdAccountsManager extends SvdManager with SvdFileEventsReactor with Logging {

    import Events._


    log.info("SvdAccountsManager (v%s) is loading".format(SvdConfig.version))

    // log.info("Registering Coreginx")
    // val coreginx = actorOf(new SvdService(SvdRootServices.coreginxConfig(), rootAccount))

    // private val accountManagers = scala.collection.mutable.Map[Int, ActorRef]() // UID => AccountManager ref

    // protected val systemPasswdFilePath = SvdConfig.systemPasswdFile // NOTE: This must be copied into value to use in pattern matching
    addShutdownHook {
        log.warn("Got termination signal. Unregistering file events")
        unregisterFileEvents(self)

        // log.info("Stopping spawned user workers")
        // userAccounts.foreach{
        //     account =>
        //         val pidFile = SvdConfig.userHomeDir / "%d".format(account.uid) / "%d.pid".format(account.uid)
        //         log.trace("PIDFile: %s".format(pidFile))
        //         if (new java.io.File(pidFile).exists) {
        //             val pid = Source.fromFile(pidFile).getLines.toList.head.trim.toInt
        //             log.debug("Client VM PID to be killed: %d".format(pid))

        //             // XXX: TODO: define death watch daemon:
        //             kill(pid)

        //             log.debug("Client VM PID file to be deleted: %s".format(pidFile))
        //             rm_r(pidFile)
        //         } else {
        //             log.warn("File not found: %s".format(pidFile))
        //         }
        // }
        log.info("All done.")
        // postStop
    }


    override def preStart = {
        super.preStart
        log.debug("SvdAccountsManager is starting.")
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
            sender ! Success
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


        case Success =>
            log.debug("Got success")


        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))
            // sender ! Error("Unknown signal %s".format(x))

    }


    def receive = awareOfUserManagers(Nil)


    override def postStop = {
        log.debug("Accounts Manager postStop.")
        super.postStop
    }

}
