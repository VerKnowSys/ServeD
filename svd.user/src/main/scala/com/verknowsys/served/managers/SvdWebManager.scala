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
import org.json4s._
import org.json4s.native._


/**
 * Web Manager - Web Panel Manager
 *
 * @author dmilith
 */
class SvdWebManager(account: SvdAccount) extends SvdManager with SvdFileEventsReactor with SvdUtils with Logging {

    val homeDir = SvdConfig.userHomeDir / account.uid.toString
    val accountsManager = context.actorFor("akka://%s@%s:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort))
    val accountManager = context.actorFor("/user/SvdAccountManager")


    addShutdownHook {
        log.warn("Shutdown hook in Web Manager")
        postStop
    }

    override def postStop = {
        log.info("Stopping Web Manager for uid: %s".format(account.uid))
        super.postStop
    }


    override def preStart = {
        super.preStart
        log.info("Starting Web Manager for uid: %s".format(account.uid))

        log.debug("Getting web panel port from AccountsManager")
        implicit val timeout = Timeout(5 seconds)
        (accountsManager ? Admin.GetPort) onSuccess {
            case webPort: Int =>
                log.trace("Got web panel port %d", webPort)

                log.debug("Launching Web Panel for UID: %d", account.uid)
                spawnServer(webPort)
        } onFailure {
            case _ =>
                val webPort = account.uid + 1027
                log.debug("Assumming headless mode")
                log.debug("Launching Web Panel for UID: %d on port: %d".format(account.uid, webPort))
                spawnServer(webPort)
        }
    }


    def receive = traceReceive {

        case Success =>
            log.debug("Success in WebManager")

        case x: Notify.Base => // forward Notify messages from web panel
            log.debug("Web Panel got a message: %s. Forwarding to Account Manager", x)
            accountManager forward x

        // case System.GetUserProcesses(x) => // it doesn't require any additional priviledges
        //     sender ! SvdLowLevelSystemAccess.usagesys(account.uid).toString // take list of user processes

        case x: System.Base =>
            log.debug("Web Panel got a message: %s. Forwarding to Account Manager", x)
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

        val server = http
            .context("/assets") {
                _.resources(base)
            }
            .filter(panel)
            .start // spawn embeded version of server

        accountManager forward Notify.Message(formatMessage("I:Your panel has been started for user: %s at: http://%s:%d".format(account.userName, currentHost.getHostAddress, port))) // XXX : hardcoded host.

        addShutdownHook {
            log.warn("Shutdown hook in Web Manager invoked")
            server.stop
        }
    }


}
