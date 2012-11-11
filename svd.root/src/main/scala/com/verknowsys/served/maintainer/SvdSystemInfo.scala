package com.verknowsys.served.maintainer


import com.verknowsys.served._
import com.verknowsys.served.api.Admin._
import com.verknowsys.served.api._
import com.verknowsys.served.utils.Logging
import scala.collection.JavaConversions._
import com.verknowsys.served.utils._

import akka.actor._


/**
 *  Basic system information
 *
 *  @author teamon
 */
class SvdSystemInfo extends SvdActor {

    log.info("Starting SvdSystemInfo")

    def receive = {
        case ListTreeActors =>
            sender ! Error("not yet implemented")
            // sender ! ActorsList(system.actorOf(Props[SupervisorActor])) //.map(ref2info))
            // XXX: CHECKME
    }

    protected def ref2info(ref: ActorRef): ActorInfo =
        ActorInfo(
            uuid            = ref.path.toString,
            className       = ref.getClass.getName,
            status          = ref.toString,
            linkedActors    = Nil //ref.linkedActors.values.toList.map(ref2info)
        )
}
