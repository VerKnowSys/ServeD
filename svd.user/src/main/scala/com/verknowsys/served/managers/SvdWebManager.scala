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


    def receive = traceReceive {
        case Init =>
            log.info("SvdWebManager received Init. Launching panel")

            log.debug("Getting web panel port from AccountsManager")
            (accountsManager ? GetPort) onSuccess {
                case webPort: Int =>
                    log.debug("Got web panel port %d", webPort)
                    context.become(started(webPort))

                    log.trace("Sending Init once again")
                    self ! Init

                case x =>
                    log.error("Web Panel failed: %s", x)
            }

        case x =>
            val m = "Unknown SvdWebManager message: %s".format(x)
            log.warn("%s".format(m))
            sender ! Error(m)

    }


    private def started(webPort: Int): Receive = traceReceive {

        case Init =>
            log.debug("Launching jetty for UID: %d", account.uid)
            sender ! Success
            web.Server(webPort) // this one is blocking

    }


}
