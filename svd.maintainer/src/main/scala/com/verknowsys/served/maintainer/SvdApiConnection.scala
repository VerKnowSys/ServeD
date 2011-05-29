package com.verknowsys.served.maintainer

import akka.actor.Actor
import akka.routing.Dispatcher
import akka.serialization.RemoteActorSerialization.toRemoteActorRefProtocol

import com.verknowsys.served.utils.Logging
import com.verknowsys.served.utils.SvdExceptionHandler
import com.verknowsys.served.api._


class SvdApiConnection extends Actor with SvdExceptionHandler {
    log.info("Starting new API connection")

    def receive = {
        case General.CreateSession =>
            log.trace("Create new session for remote client")
            self.reply(toRemoteActorRefProtocol(self.spawnLink[SvdApiSession]))

       case msg =>
            log.warn("Message " + msg + " not recognized")
    }
}
