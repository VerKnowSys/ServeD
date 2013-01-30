/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.maintainer


import com.verknowsys.served.api.Admin._
import com.verknowsys.served.api._
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
            sender ! ApiError("not yet implemented")
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
