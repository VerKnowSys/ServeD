package com.verknowsys.served.utils

import akka.actor.ActorRef

/**
 * Handle global reference to actor
 *
 * Usage:
 *    object MyGlobal extends GlobalActorRef(Actor.registry.actorFor[MyActor])
 *    object MyGlobal extends GlobalActorRef(Some(Actor.remove.actorFor[MyActor]))
 *
 * @author teamon
 */
abstract class GlobalActorRef(actorFun: => Option[ActorRef]) {
    lazy val actor = actorFun

    def !(message: Any)(implicit sender: Option[ActorRef] = None) = actor.foreach(_ ! message)
    def !!(message: Any)(implicit sender: Option[ActorRef] = None) = actor.flatMap(_ !! message)
}
