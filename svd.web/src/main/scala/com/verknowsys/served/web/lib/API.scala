package com.verknowsys.served.web.lib

import akka.actor.{Actor, ActorRef}

import com.verknowsys.served.api._
import com.verknowsys.served.utils.Logging

object API extends Logging {
    final val host = "localhost" // TODO: Use some configuration
    final val port = 10 // 2011-06-09 02:01:28 - dmilith - TODO: XXX: should use config value for standard value of API port

    lazy val svd = {
        val session = RemoteSession(host, port)
        if(!session.isDefined) log.error("ServeD not found at %s:%d", host, port)
        session.get // HACK: Handle error!!
    }

    def request[T](msg: ApiMessage)(f: PartialFunction[Any, T]): Option[T] = {
        login // XXX: Temporary code!

        log.debug("API REQUEST: %s", msg)

        val response = svd !! msg
        log.debug("API RESPONSE: %s", response)

        response match {
            case Some(response) if f.isDefinedAt(response) => Some(f(response))
            case Some(response) => None //logger.error("[akka] Unhandled reponse %s", response)
            case None => None//logger.error("[akka] Connection timeout")
        }
    }

    def !(msg: ApiMessage){
        request(msg){
            case Success => // TODO: Do something with that
            case _ => // TODO: Do something with that
        }
    }

    // XXX: Very temporary stuff
    def login {
        (svd !! General.GetStatus) match {
            case Some(r) => r match {
                case General.Status.Connected =>
                    log.info("Already connected")

                case General.Status.Disconnected =>
                    log.info("Not connected. Connecting...")

                    (svd !! General.Connect(501)) match {
                        case Some(response) => response match {
                            case Success => log.info("Logged in")
                            case res => log.error("Failed to log in. Response: %s", res)
                        }
                        case res => log.error("Failed to log in. Response: %s", res)
                    }
            }
            case None => log.error("No response from svd")
        }
    }
}
