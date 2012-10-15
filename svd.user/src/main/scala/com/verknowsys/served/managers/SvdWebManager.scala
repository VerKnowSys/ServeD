package com.verknowsys.served.managers

import com.verknowsys.served._
import com.verknowsys.served.services._
// import com.verknowsys.served.LocalAccountsManager
import com.verknowsys.served.api._
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.git._
import com.verknowsys.served.db.{DBServer, DBClient, DB}
import com.verknowsys.served.utils._
import com.verknowsys.served.web._
import com.verknowsys.served.web.router._
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

import unfiltered.util._
import unfiltered.Cookie
import unfiltered.request._
import unfiltered.response._
import unfiltered.kit._
import unfiltered.jetty.Http
import java.net.URL
import unfiltered.filter.Plan
import net.liftweb.json._


/**
 * Web Manager - Web Panel Manager
 *
 * @author dmilith
 */
class SvdWebManager(account: SvdAccount) extends SvdManager with SvdFileEventsReactor with SvdUtils {

    val homeDir = SvdConfig.userHomeDir / account.uid.toString
    val accountsManager = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode
    val accountManager = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountManager".format(SvdConfig.served, account.accountManagerPort))


    override def postStop = {
        log.info("Stopping Web Manager for uid: %s".format(account.uid))
        super.postStop
    }


    override def preStart = {
        super.preStart
        log.info("Starting Web Manager for uid: %s".format(account.uid))

        log.debug("Getting web panel port from AccountsManager")
        (accountsManager ? Admin.GetPort) onSuccess {
            case webPort: Int =>
                log.trace("Got web panel port %d", webPort)

                log.debug("Launching Web Panel for UID: %d", account.uid)
                spawnServer(webPort)
        }
    }


    def receive = traceReceive {

        case Success =>
            log.debug("Success in WebManager")

        case x: Notify.Message => // forward Notify messages from web panel
            log.debug("Web Panel got a message: %s. Forwarding to Account Manager", x)
            accountManager forward x

        case x: UserWeb.RegisterDomain =>
            log.debug("Web Panel requested RegisterDomain. Forwarding to Account Manager")
            accountManager forward x

        case x =>
            val m = "Unknown SvdWebManager message: %s".format(x)
            log.warn("%s".format(m))
            sender ! Error(m)

    }


    def spawnServer(port: Int) = {
        val base = new URL(getClass.getResource("/public/"), ".")
        val http = Http(port)
        val panel = new SvdAccountPanel(self, account, port)

        http
            .context("/assets") {
                _.resources(base)
            }
            .filter(panel)
            .start // spawn embeded version of server

        self ! Notify.Message("Your panel has been started for user: %s at: http://%s:%d".format(account.userName, "127.0.0.1", port)) // XXX : hardcoded host.
    }


}
