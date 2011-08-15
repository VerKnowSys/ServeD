package com.verknowsys.served.maintainer

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.{remote, actorOf, registry}
import akka.routing.Dispatcher

import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import com.verknowsys.served.managers._



class SvdApiSession extends SvdManager {
    log.info("Starting new API session")

    private var manager: Option[ActorRef] = None // XXX: Var

    override def receive = {
        case General.GetStatus =>
            self reply General.Status.Disconnected

        case General.Connect(userUid) =>
            log.trace("Remote client trying to connect with UID: %s", userUid)

            (SvdAccountsManager !! GetAccountManager(userUid)) match {
                case Some(m: ActorRef) =>
                    manager = Some(m)
                    become(dispatch)
                    self reply Success
                    log.info("Remote client successfully connected with UID: %s", userUid)

                case _ =>
                    log.error("User with UID: '%d' not found", userUid)
                    self reply Error("User with UID: '%s' not found".format(userUid))
            }
    }

    // lazy val loggingManagers = remote.actorFor("service:logging-manager", "localhost", 8000) :: Nil  // XXX: HACK: should use account.servicePort instead of 8000

    protected def dispatch: Receive = traceReceive {
        case General.GetStatus =>
            self reply General.Status.Connected

        case msg: Logger.Base =>
            log.debug("Remote client sent %s. Forwarding to LoggingManager", msg)

            // temporary!
            // loggingManagers.foreach(_ ! msg) // disabled due to 8000 port issue

            registry.actorFor[LoggingManager].foreach { _ forward msg }

        case msg: Admin.Base =>
            log.debug("Remote client sent %s. Forwarding to SvdSystemInfo", msg)
            registry.actorFor[SvdSystemInfo].foreach { _ forward msg }

        case msg if manager.isDefined =>
            log.debug("Remote client sent %s. Forwarding to AccountManager (%s)", msg, manager)
            manager.foreach { _ forward msg }
    }
}

