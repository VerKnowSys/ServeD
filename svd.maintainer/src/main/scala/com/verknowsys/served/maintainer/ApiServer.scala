package com.verknowsys.served.maintainer

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.{actorOf, registry}
import akka.routing.Dispatcher
import akka.util.Logging

import com.verknowsys.served.api._
import com.verknowsys.served.managers._


class ApiSession extends Actor with Dispatcher {
    private var manager: Option[ActorRef] = None // XXX: Var
    
    override def receive = {
        case General.Connect(username) =>
            log.trace("Remote client trying to connect with username %s", username)

            (registry.actorFor[AccountsManager] flatMap (_ !! GetAccountManager(username))) match {
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
        case msg if manager.isDefined => 
            log.debug("Remote client sent %s. Forwarding to AccountManager", msg)
            manager.get
    }
}

object ApiServer {
    final val host = "localhost"
    final val port = 5555
    
    def start {
        Actor.remote.start(host, port)
        Actor.remote.registerPerSession("service:api", actorOf[ApiSession])
    }
}
