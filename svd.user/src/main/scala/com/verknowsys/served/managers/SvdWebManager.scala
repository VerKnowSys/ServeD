/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.managers

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import com.verknowsys.served.web.api._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import unfiltered.jetty.Http
import java.net.URL


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
        val sgp = (accountsManager ? System.GetPort)
        sgp onSuccess {
            case webPort: Int =>
                log.trace("Got web panel port %d", webPort)

                log.debug("Launching Web Panel for UID: %d", account.uid)
                spawnServer(webPort)
        }
        sgp onFailure {
            case _ =>
                val webPort = account.uid + 1027
                log.debug("Assumming headless mode")
                log.debug("Launching Web Panel for UID: %d on port: %d".format(account.uid, webPort))
                spawnServer(webPort)
        }
    }


    def receive = traceReceive {

        case ApiSuccess =>
            log.debug("ApiSuccess in WebManager")

        case ApiError(x) =>
            log.warn("Error occured: %s", x)

        case x: Notify.Base => // forward Notify messages from web panel
            log.debug("Web Panel got a message: %s. Forwarding to Account Manager", x)
            accountManager forward x

        // case System.GetUserProcesses(x) => // it doesn't require any additional priviledges
        //     sender ! SvdLowLevelSystemAccess.usagesys(account.uid).toString // take list of user processes

        case x: Security.Base =>
            accountManager forward x

        case x: Maintenance.Base =>
            accountManager forward x

        case x: User.Base =>
            accountManager forward x

        case x: Admin.Base =>
            accountManager forward x

        case x: System.Base =>
            log.debug("Web Panel got a message: %s. Forwarding to Account Manager", x)
            accountManager forward x

        case x =>
            val m = "Unknown SvdWebManager message: %s".format(x)
            log.warn("%s".format(m))
            sender ! ApiError(m)

    }


    def spawnServer(port: Int) = {
        val base = new URL(getClass.getResource("/public/"), ".")
        val http = Http(port)

        lazy val postAPI = new SvdPOST(self, account, port)
        lazy val getAPI = new SvdGET(self, account, port)

        val server = http
            .context("/assets") {
                _.resources(base)
            }
            .filter(postAPI)
            .filter(getAPI)
            .start // spawn embeded version of server

        accountManager forward Notify.Message(
            formatMessage("W:Your panel has been started for user: %s at: http://%s:%d".format(
                account.userName, currentVPNHost, port))) // XXX : hardcoded host.

        addShutdownHook {
            log.warn("Shutdown hook in Web Manager invoked")
            server.stop
        }
    }


}
