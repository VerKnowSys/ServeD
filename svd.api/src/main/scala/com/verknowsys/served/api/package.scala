/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served

import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.actor._
// import akka.serialization.RemoteActorSerialization.fromBinaryToRemoteActorRef
// import akka.remote.protocol.RemoteProtocol.RemoteActorRefProtocol


package object api {
    val system = ActorSystem.create
    implicit val timeout = Timeout(20 seconds)

    def RemoteSession(host: String, port: Int): Option[ActorRef] = {
        val svd = system.actorOf(Props[Actor], name = "service:api") // host, port
        val future = (svd ? General.CreateSession) map {
            case Some(x: ActorRef) =>
                println("Found remote session to host/port: %s/%d".format(host, port))
                return Some(x)
            case _ =>
                println("Failed remote session to host/port: %s/%d".format(host, port))
        }
        None
        // val res = Await.result(future, timeout.duration).asInstanceOf[ActorRef]
        // println("remote session to host/port: %s/%d".format(host, port))
        // Some(res)
        // XXX: CHECKME

        //  match {
        //     case protocol: RemoteActorRefProtocol =>
        //         fromBinaryToRemoteActorRef(protocol.toByteArray)
        // }
    }

    type UUID = java.util.UUID
    def randomUUID = java.util.UUID.randomUUID
}
