// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.api._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.Logging
// import com.verknowsys.served.utils.GlobalActorRef

import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import akka.actor._
import com.sun.jna.{Native, Library}
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import org.webbitserver._
import org.webbitserver.handler._


// object SvdSystemManager //extends GlobalActorRef(actorOf[SvdSystemManager])


/**
*   @author dmilith
*
*   SvdSystemManager - responsible for System managment and monitoring
*/
class SvdSystemManager extends SvdExceptionHandler {

    log.info("SvdSystemManager is loading")


    override def preStart = {
        super.preStart
        log.debug("SvdSystemManager ready")

        if (isBSD)
            log.warn("SYSUSAGE: " + SvdLowLevelSystemAccess.usagesys(0))

        log.debug("Updating system time")
        SvdNtpSync()

        log.info("Spawning Webbit WebSockets Server")
        val webServer = WebServers.createWebServer(60006)
          .add("/livemonitor", new SvdWebSocketsHandler)
          // .add(new StaticFileHandler("/web"))
          .start.get
        log.info("WebSockets Server running at " + webServer.getUri)
    }


    def receive = {

        case GetUserProcesses(uid: Int) =>
            log.debug("Gathering user processes of %s".format(uid))
            if (isBSD)
                sender ! SvdLowLevelSystemAccess.usagesys(uid)
            else
                sender ! Error("NOT-IMPLEMENTED on this OS! Do it on FreeBSD host.")


        case GetNetstat =>
            sender ! Error("Not implemented")
            // XXX: NOTE: TODO: this function causes SIGSEGV on FreeBSD. This requires some investigation!

            // SvdLowLevelSystemAccess.netstat.stat(SvdLowLevelSystemAccess.core)
            // log.warn("Network usage (bytes): IN: %s, OUT: %s".format(SvdLowLevelSystemAccess.netstat.getTcpInboundTotal, SvdLowLevelSystemAccess.netstat.getTcpOutboundTotal))
            // self reply Success

        case Quit =>
            log.info("Quitting SvdSystemManager")
            sys.exit(0)

        case Chown(what, userId, recursive) =>
            log.debug("Chown called on location: '%s' with uid: %s, recursive: %s".format(what, userId, recursive))
            chown(what, userId, SvdConfig.defaultUserGroup, recursive)
            sender ! Success

        case Chmod(what, mode, recursive) =>
            log.debug("Chmod called on location: '%s' with mode: %s (recursively: %s)".format(what, mode, recursive))
            chmod(what, mode, recursive)
            sender ! Success

        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))
            sender ! Error("Unknown signal %s".format(x))

    }


    override def toString = "SvdSystemManager"


}
