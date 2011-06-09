package com.verknowsys.served.web.lib

import net.liftweb.http.SessionVar

import akka.actor.{Actor, ActorRef}

import com.verknowsys.served.utils.Logging
import com.verknowsys.served.api._

object Session {
    object Username extends SessionVar[String]("")
    
    object api {
        final val host = "localhost" // TODO: Use some configuration
        final val port = 10 // 2011-06-09 02:01:28 - dmilith - TODO: XXX: should use config value for standard value of API port

        object svd extends SessionVar[ActorRef](service)

        def request[T](msg: ApiMessage)(f: PartialFunction[Any, T]): Option[T] = {
            (svd.get !! msg) match {
                case Some(response) if f.isDefinedAt(response) => Some(f(response))
                case Some(response) => None //logger.error("[akka] Unhandled reponse %s", response)
                case None => None//logger.error("[akka] Connection timeout")
            }
        }
        
        protected def service = RemoteSession(host, port).get // HACK: Handle error!!
    }

    def login(username: String, password: String) = {
        api.request(General.Connect(username)) {
            case Success =>
                Username.set(username)
            case Error =>
                //logger.error("Failed to log in")
        }.isDefined
    }
    
    def logout = Username.set("")
    
    def authorized = {
        // TODO: Add some real validation. Temporary code so there is no need to log in on every code reload 
        // (jetty restarts app and session vars are gone)
        if(Username.get == ""){
            login(System.getProperty("user.name"), "")
        }
        Username.get != "" // change me!
    }
}

