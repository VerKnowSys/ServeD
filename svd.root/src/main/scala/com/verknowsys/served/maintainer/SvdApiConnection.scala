package com.verknowsys.served.maintainer

import akka.actor.Actor
// import akka.serialization.RemoteActorSerialization.toRemoteActorRefProtocol

import com.verknowsys.served.utils.Logging
import com.verknowsys.served.utils.SvdExceptionHandler
import com.verknowsys.served.api._


class SvdApiConnection extends Actor with SvdExceptionHandler {
    log.info("Starting new API connection")

    def receive = {
        case General.CreateSession =>
            log.debug("Create new session for remote client")
            // XXX: CHECKME
            // sender ! SvdApiSession
            // sender linkWith SvdApiSession
    }
}
