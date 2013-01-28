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

import akka.pattern.ask
import akka.util
import akka.util.Timeout
import scala.util._
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global


class SvdNotificationCenter(account: SvdAccount) extends SvdActor with Logging {

    implicit val timeout = Timeout(SvdConfig.defaultAPITimeout/1000 seconds)


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
        gates.par.map {
            gate =>
                (gate ? Notify.Connect) onComplete {

                    case Success(content) =>
                        log.info("Notifications Center started")

                    case Failure(exception) =>
                        log.error("Exception: %s", exception)

                }
        }
    }


    override def postStop {
        log.debug("Shutting down Notification Center. Disconnecting all gates.")
        gates.par.foreach(_ ! Notify.Disconnect)
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
            log.trace(s"Setting status ${status}")
            gates.par.map{
                gate =>
                    log.debug("Setting Status for gate: %s", gate)
                    (gate ? Notify.Status(status)) onComplete {
                        case Success(some) =>
                            log.debug("Status set to %s", some)
//                            sender ! ApiSuccess

                        case Failure(exception) =>
                            log.error("Exception: %s", exception)
//                            sender ! Error("Exception: %s", exception)

                    }
            }




        case Notify.Message(msg) =>
            log.trace("Sending message %s", msg)
            gates.par.map{
                gate =>
                    log.debug("Setting Message for gate: %s", gate)
                    (gate ? Notify.Message(msg)) onComplete {
                        case Success(some) =>
                            log.debug("Sending message :%s", some)
                            sender ! ApiSuccess

                        case Failure(exception) =>
                            log.warn(s"Failed to send notification with message: ${msg} caused by: ${exception}")
                            // sender ! Failure

                    }
            }


        case x =>
            log.debug("Unknown message for notification center: %s", x)
        //     accountManager forward x

    }


}

