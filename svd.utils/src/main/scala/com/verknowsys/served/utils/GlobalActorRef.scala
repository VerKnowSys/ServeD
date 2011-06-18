package com.verknowsys.served.utils

import akka.actor.ActorRef

abstract class GlobalActorRef(actor: => Option[ActorRef]) {
    def !(message: Any)(implicit sender: Option[ActorRef] = None) = actor.map(_ ! message)
    def !!(message: Any)(implicit sender: Option[ActorRef] = None) = actor.map(_ !! message)
}
