package com.verknowsys.served.notifications

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.Logging

import akka.actor.Actor


class SvdNotificationCenter(account: SvdAccount) extends SvdExceptionHandler with SvdUtils with Logging {
    log.info("SvdNotificationCenter is loading")

    val gates: List[Gate] = new SvdXMPPGate(
        host = SvdConfig.notificationXmppHost,
        port = SvdConfig.notificationXmppPort,
        login = SvdConfig.notificationXmppLogin,
        password = SvdConfig.notificationXmppPassword,
        resource = SvdConfig.notificationXmppResource
    ) :: Nil

    override def preStart {
        gates.foreach(_.connect)
        super.preStart
    }


    def receive = {

        case Init =>
            log.info("Initializing Notification Center")
            sender ! Success

        case Notify.Status(status) =>
            log.debug("Setting status %s", status)
            gates.foreach(_ setStatus status)

        case Notify.Message(msg) =>
            log.debug("Sending message %s", msg)
            gates.foreach(_ send msg)
    }

    override def postStop {
        log.debug("Shutting down Notification Center. Disconnecting all gates.")
        gates.foreach(_.disconnect)
        super.postStop
    }
}

