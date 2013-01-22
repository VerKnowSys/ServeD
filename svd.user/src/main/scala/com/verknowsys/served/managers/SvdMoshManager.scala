/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.managers


import scala.io.Source
import akka.actor._
import akka.pattern.AskTimeoutException
import akka.pattern.ask
import akka.util
import akka.util.Timeout
import scala.util._
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import com.verknowsys.served.services._


class SvdMoshManager(account: SvdAccount) extends Actor with Logging with SvdActor with SvdUtils {


    override def preStart = {
        log.info("Launching SvdMoshManager with no sessions")
        context.become(availableWith(Nil))
    }


    def receive = {
        case _ =>
            log.warn("Some bad message in SvdMoshManager")
    }


    def availableWith(activeSessions: List[ActorRef]): Receive = {

        case User.MoshAuth =>
            log.debug("Got MoshAuth message in Mosh manager.")
            log.info("Becoming aware of new Mosh Server")
            val moshUUID = newUuid
            val moshServer = context.actorOf(Props(new SvdService("Mosh", account)), s"MoshSession-${moshUUID}")
            val future = (moshServer ? Notify.Ping)
            future onSuccess {
                case _ =>
                    context.watch(moshServer)
                    context.become(availableWith(moshServer :: activeSessions))

                    val contentFile = SvdConfig.userHomeDir / s"${account.uid}" / SvdConfig.softwareDataDir / "Mosh" / "client.string.txt" // XXX: hardcoded client string file name
                    val moshClientString = Source.fromFile(contentFile).mkString.trim
                    sender ! s"""{"message": "Mosh Session Created", "content": "${moshClientString}"}"""
            }


        case Shutdown =>
            log.debug("Got Shutdown in SvdMoshManager")
            log.info("Stopping SvdMoshManager and all related Mosh services")
            activeSessions.map{
                mosh =>
                    log.trace(s"Stopping Mosh session: ${mosh}")
                    context.unwatch(mosh)
                    context.stop(mosh)
            }
            sender ! ApiSuccess

    }


}