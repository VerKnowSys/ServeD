/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.notifications

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.Logging
import akka.actor.{Props, ActorRef}


class SvdNotificationCenter(account: SvdAccount) extends SvdActor with Logging {

    val accountManager = context.actorFor("/user/SvdAccountManager")


    val gates: List[ActorRef] = context.actorOf(Props(new SvdXMPPGate(
            host = SvdConfig.notificationXmppHost,
            port = SvdConfig.notificationXmppPort,
            login = SvdConfig.notificationXmppLogin,
            password = SvdConfig.notificationXmppPassword,
            resource = SvdConfig.notificationXmppResource,
            account = account,
            accountManager = accountManager
        ))) :: context.actorOf(Props(new SvdIRCGate(account))) :: Nil


    override def preStart {
        super.preStart
        log.info("SvdNotificationCenter is loading.")
        log.debug("Sending Notify.Connect signal to all registered Gates.")
        gates.foreach(_ ! Notify.Connect)
    }


    override def postStop {
        log.debug("Shutting down Notification Center. Disconnecting all gates.")
        gates.foreach(_ ! Notify.Disconnect)
        super.postStop
    }


    override def preRestart(reason: Throwable, message: Option[Any]) {
        log.debug("preRestart down Notification Center with reason: %s and message: %s", reason, message)
        super.preRestart(reason, message)
    }


    addShutdownHook {
        val thirtyPercentLessTimeout = SvdConfig.shutdownTimeout - SvdConfig.shutdownTimeout/3
        log.warn("Notification Center shutdown hook invoked. Waiting %s seconds for some late messangers before stopping".format(thirtyPercentLessTimeout))
        Thread.sleep(thirtyPercentLessTimeout)
        postStop
    }


    def receive = {

        case Notify.Status(status) =>
            log.trace("Setting status %s", status)
            gates.foreach(_ ! Notify.Status(status))
            sender ! Success

        case Notify.Message(msg) =>
            log.trace("Sending message %s", msg)
            gates.foreach(_ ! Notify.Message(msg))
            sender ! Success

        case x =>
            log.debug("Unknown message for notification center: %s", x)
        //     accountManager forward x

    }


}

