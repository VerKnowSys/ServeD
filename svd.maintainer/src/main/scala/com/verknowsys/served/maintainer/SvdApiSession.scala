package com.verknowsys.served.maintainer

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.{actorOf, registry}
import akka.routing.Dispatcher

import com.verknowsys.served.utils.Logging
import com.verknowsys.served.utils.SvdExceptionHandler
import com.verknowsys.served.utils.LoggingManager
import com.verknowsys.served.api._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.systemmanager.managers._


class SvdApiSession extends Actor with Dispatcher with SvdExceptionHandler {
    log.info("Starting new API session")

    private var manager: Option[ActorRef] = None // XXX: Var

    override def receive = {
        case General.Connect(username) =>
            log.trace("Remote client trying to connect with username %s", username)

            (registry.actorFor[SvdAccountsManager] flatMap (_ !! GetAccountManager(username))) match {
                case Some(m: ActorRef) =>
                    manager = Some(m)
                    become(dispatch)
                    self reply Success
                    log.trace("Remote client successfully connected with username %s", username)

                case Some(e: Error) => 
                    self reply e

                case _ =>
                    self reply Error("User with name '%s' not found".format(username))
            }
    }

    protected def routes = {
        case msg: Logger.Base =>
            log.debug("Remote client sent %s. Forwarding to LoggingManager", msg)
            registry.actorFor[LoggingManager].get
            
        case msg: Admin.Base =>
            log.debug("Remote client sent %s. Forwarding to SvdSystemInfo", msg)
            registry.actorFor[SvdSystemInfo].get

        case msg if manager.isDefined => 
            log.debug("Remote client sent %s. Forwarding to AccountManager", msg)
            manager.get
    }
}
