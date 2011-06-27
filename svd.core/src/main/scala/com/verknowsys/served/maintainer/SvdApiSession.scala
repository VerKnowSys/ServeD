package com.verknowsys.served.maintainer

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.{remote, actorOf, registry}
import akka.routing.Dispatcher

import com.verknowsys.served.utils.Logging
import com.verknowsys.served.utils.SvdExceptionHandler
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
                case Some(res) => res match {
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

                case _ =>
                    self reply Error("User with name '%s' not found".format(username))
            }
    }
    
    // lazy val loggingManagers = remote.actorFor("service:logging-manager", "localhost", 8000) :: Nil  // XXX: HACK: should use account.servicePort instead of 8000

    protected def routes = {
        case msg: Logger.Base =>
            log.debug("Remote client sent %s. Forwarding to LoggingManager", msg)
            
            // temporary!
            // loggingManagers.foreach(_ ! msg) // disabled due to 8000 port issue 
            
            registry.actorFor[LoggingManager].get
            
        case msg: Admin.Base =>
            log.debug("Remote client sent %s. Forwarding to SvdSystemInfo", msg)
            registry.actorFor[SvdSystemInfo].get

        case msg if manager.isDefined => 
            log.debug("Remote client sent %s. Forwarding to AccountManager", msg)
            manager.get
    }
}

