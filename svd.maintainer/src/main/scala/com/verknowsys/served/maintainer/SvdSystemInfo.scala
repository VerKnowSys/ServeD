package com.verknowsys.served.maintainer


import com.verknowsys.served.api.Admin._

// akka
import akka.actor.{Actor, ActorRef, SupervisorActor}


/**
 *  Basic system information
 *
 *  @author teamon
 */
class SvdSystemInfo extends Actor {
    log.trace("Started SvdSystemInfo")
    
    def receive = {
        case ListActors =>
            self reply ActorsList(Actor.registry.actorsFor[SupervisorActor].map(ref2info))
    }
    
    protected def ref2info(ref: ActorRef): ActorInfo = 
        ActorInfo(ref.uuid.toString, ref.actorClassName, ref.isRunning.toString, ref.linkedActors.map(ref2info))
}
