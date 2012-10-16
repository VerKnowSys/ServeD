package com.verknowsys.served.notifications

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.Logging

import akka.actor.Actor


class SvdNotificationCenter(account: SvdAccount) extends SvdExceptionHandler with SvdUtils with Logging {

    val gates: List[Gate] =
        new SvdXMPPGate(
            host = SvdConfig.notificationXmppHost,
            port = SvdConfig.notificationXmppPort,
            login = SvdConfig.notificationXmppLogin,
            password = SvdConfig.notificationXmppPassword,
            resource = SvdConfig.notificationXmppResource
        ) :: Nil // new SvdMailGate :: Nil


    override def preStart {
        super.preStart
        log.info("SvdNotificationCenter is loading")
        gates.foreach(_.connect)
    }


    override def preRestart(reason: Throwable, message: Option[Any]) {
        log.debug("preRestart down Notification Center with reason: %s and message: %s", reason, message)
        super.preRestart(reason, message)
    }


    override def postStop {
        log.debug("Shutting down Notification Center. Disconnecting all gates.")
        gates.foreach(_.disconnect)
        super.postStop
    }


    def receive = {

        case Notify.Status(status) =>
            log.debug("Setting status %s", status)
            gates.foreach(_ setStatus status)
            sender ! Success

        case Notify.Message(msg) =>
            log.debug("Sending message %s", msg)
            gates.foreach(_ send msg)
            sender ! Success

        case x =>
            log.debug("Unknown message: %s", x)
            sender ! Success

    }


}

