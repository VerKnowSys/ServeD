package com.verknowsys.served.notifications

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.Logging

import akka.actor.Actor


class SvdNotificationCenter(account: SvdAccount) extends SvdActor with SvdUtils with Logging {

    val accountManager = context.actorFor("/user/SvdAccountManager")

    val gates: List[Gate] =
        new SvdXMPPGate(
            host = SvdConfig.notificationXmppHost,
            port = SvdConfig.notificationXmppPort,
            login = SvdConfig.notificationXmppLogin,
            password = SvdConfig.notificationXmppPassword,
            resource = SvdConfig.notificationXmppResource,
            account = account,
            accountManager = accountManager
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


    addShutdownHook {
        val thirtyPercentLessTimeout = SvdConfig.shutdownTimeout - SvdConfig.shutdownTimeout/3
        log.warn("Notification Center shutdown hook invoked. Waiting %s seconds for some late messangers before stopping".format(thirtyPercentLessTimeout))
        Thread.sleep(thirtyPercentLessTimeout)
        postStop
    }


    def receive = {

        case Notify.Status(status) =>
            log.trace("Setting status %s", status)
            gates.foreach(_ setStatus status)
            sender ! Success

        case Notify.Message(msg) =>
            log.trace("Sending message %s", msg)
            gates.foreach(_ send msg)
            sender ! Success

        case x =>
            log.debug("Unknown message for notification center: %s", x)
        //     accountManager forward x

    }


}

