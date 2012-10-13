package com.verknowsys.served.managers

import com.verknowsys.served._
import com.verknowsys.served.services._
// import com.verknowsys.served.LocalAccountsManager
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api._
import com.verknowsys.served.db.{DBServer, DBClient, DB}
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.notifications._

import java.security.PublicKey
import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import akka.actor._


/**
 * Web Manager - Web Panel Manager
 *
 * @author dmilith
 */
class SvdWebManager(val account: SvdAccount) extends SvdExceptionHandler with SvdFileEventsReactor with SvdUtils {

    log.info("Starting Web Manager for uid: %s".format(account.uid))

    val homeDir = SvdConfig.userHomeDir / account.uid.toString
    val accountsManager = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode
    val accountManager = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountManager".format(SvdConfig.served, account.accountManagerPort))


    override def postStop = {
        super.postStop
    }

    override def preStart = {
        super.preStart
        log.info("Launching SvdWebManager")
        log.debug("Getting web panel port from AccountsManager")
        (accountsManager ? GetPort) onSuccess {
            case webPort: Int =>
                log.trace("Got web panel port %d", webPort)

                (accountManager ? Notify.Message("Web panel started for you on port: %d".format(webPort))
                ) onSuccess {
                    case _ =>
                        log.debug("Success notifying")
                } onFailure {
                    case x =>
                        log.debug("Failure: %s", x)
                }
                // context.become(started(webPort))
                log.debug("Launching jetty for UID: %d", account.uid)
                sender ! Success
                web.Server(webPort) // this one is blocking

            case x =>
                log.error("Web Panel failed: %s", x)
        }
    }


    def receive = traceReceive {

        case Success =>
            log.debug("Success in WebManager")

        case x =>
            val m = "Unknown SvdWebManager message: %s".format(x)
            log.warn("%s".format(m))
            sender ! Error(m)

    }



}
