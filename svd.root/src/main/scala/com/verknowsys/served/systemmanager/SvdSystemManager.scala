/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.systemmanager


import com.verknowsys.served._
import com.verknowsys.served.managers._
import com.verknowsys.served.utils._
import com.verknowsys.served.api._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.Logging

import akka.actor._
import org.webbitserver._
import org.json4s.native.JsonMethods._


/**
*   SvdSystemManager - responsible for System managment and monitoring
*
*   @author dmilith
*/
class SvdSystemManager extends SvdManager with Logging {

    SvdLowLevelSystemAccess


    override def preStart = {
        super.preStart
        log.info("SvdSystemManager is loading")

        log.debug("Checking required paths…")
        checkOrCreateDir(SvdConfig.systemHomeDir / SvdConfig.softwareDataDir)

        log.debug("Updating system time")
        SvdNtpSync

        log.info("Spawning Webbit WebSockets Server")
        val webServer = WebServers.createWebServer(60006)
          .add("/livemonitor", new SvdWebSocketsHandler)
          // .add(new StaticFileHandler("/web"))
          .start.get
        log.info("WebSockets Server running at " + webServer.getUri)
    }


    def receive = {

        case Security.GetAccountPriviledges(account) =>
            new SvdAccountSecurityCheck(account).load match {
                case Some(container) =>
                    val privs = (parse(container) \ "privdgs").children.map {
                        child => compact(render(child))
                    }
                    log.info("Priviledges found for account: %s. Granting access for: %s", account.userName, privs)
                    sender ! """{"message": "Security check passed.", "status": 0, "content": %s}""".format(container)

                case None =>
                    sender ! ApiError("Priviledges Check Failed")
            }

        case System.RegisterDomain(domain, actorProxy) =>
            log.info("Registering domain: %s", domain)

            validateDomain(domain) match {
                case None => // no errors detected
                    actorProxy ! User.StoreUserDomain(domain)
                    sender ! ApiSuccess
                    log.info("Domain registration succeeded")

                case Some(x) =>
                    sender ! ApiError("Domain registration failures: %s".format(x))
            }


        case System.GetUserProcesses(uid) =>
            log.debug("Gathering user processes of %s".format(uid))
            sender ! SvdLowLevelSystemAccess.usagesys(uid)


        case System.RegisterUserPort =>
            val newFreeLocalPort = SvdAccountUtils.randomFreePort
            log.debug(s"Trying to get port from System Manager side: ${newFreeLocalPort}")
            sender ! newFreeLocalPort


        case System.GetNetstat =>
            sender ! ApiError("Not implemented")
            // XXX: NOTE: TODO: this function causes SIGSEGV on FreeBSD. This requires some investigation!

            // SvdLowLevelSystemAccess.netstat.stat(SvdLowLevelSystemAccess.core)
            // log.warn("Network usage (bytes): IN: %s, OUT: %s".format(SvdLowLevelSystemAccess.netstat.getTcpInboundTotal, SvdLowLevelSystemAccess.netstat.getTcpOutboundTotal))
            // self reply ApiSuccess

        // case Quit =>
        //     log.info("Quitting SvdSystemManager")
        //     sys.exit(0)

        case System.Chown(what, userId, recursive) =>
            log.debug("Chown called on location: '%s' with uid: %s, recursive: %s".format(what, userId, recursive))
            chown(what, userId, SvdConfig.defaultUserGroup, recursive)
            sender ! ApiSuccess

        case System.Chmod(what, mode, recursive) =>
            log.debug("Chmod called on location: '%s' with mode: %s (recursively: %s)".format(what, mode, recursive))
            chmod(what, mode, recursive)
            sender ! ApiSuccess

        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))
            // sender ! ApiError("Unknown signal %s".format(x))

    }


    override def toString = "SvdSystemManager"


}
