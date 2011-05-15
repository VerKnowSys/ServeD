package com.verknowsys.served

import akka.serialization.RemoteActorSerialization.fromBinaryToRemoteActorRef
import akka.remote.protocol.RemoteProtocol.RemoteActorRefProtocol
import akka.actor.{Actor, ActorRef}

package object api {
    def RemoteSession(host: String, port: Int): Option[ActorRef] = {
        val svd = Actor.remote.actorFor("service:api", host, port)
        (svd !! General.CreateSession) collect {
            case protocol: RemoteActorRefProtocol =>
                fromBinaryToRemoteActorRef(protocol.toByteArray)
        }
    }
}
