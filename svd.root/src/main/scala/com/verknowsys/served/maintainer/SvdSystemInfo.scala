package com.verknowsys.served.maintainer


import com.verknowsys.served.api.Admin._
import com.verknowsys.served.utils.Logging
import scala.collection.JavaConversions._
import com.verknowsys.served.utils._

import akka.actor.{Actor, ActorRef, SupervisorActor}


/**
 *  Basic system information
 *
 *  @author teamon
 */
class SvdSystemInfo extends SvdExceptionHandler {
    log.info("Starting SvdSystemInfo")
    
    def receive = {
        case ListTreeActors =>
            self reply ActorsList(Actor.registry.actorsFor[SupervisorActor].map(ref2info))
    }
    
    protected def ref2info(ref: ActorRef): ActorInfo = 
        ActorInfo(
            uuid            = ref.uuid.toString,
            className       = ref.actorClassName,
            status          = ref.isRunning.toString,
            linkedActors    = ref.linkedActors.values.toList.map(ref2info)
        )
}
