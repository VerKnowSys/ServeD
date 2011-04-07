package com.verknowsys.served.maintainer


import com.verknowsys.served.api.Admin._

// akka
import akka.actor.{Actor, ActorRef}


/**
 *  Basic system information
 *
 *  @author teamon
 */
class SvdSystemInfo extends Actor {
    log.trace("Started SvdSystemInfo")
    
    def receive = {
        case ListActors =>
            self reply ActorsList(Actor.registry.actors.map(ref2info))
    }
    
    protected def ref2info(ref: ActorRef): ActorInfo = 
        ActorInfo(ref.uuid.toString, ref.actorClassName, ref.homeAddress, ref.isRunning.toString, ref.supervisor.map(_.uuid.toString), ref.mailboxSize)
}
