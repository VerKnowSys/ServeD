package com.verknowsys.served.playground

import akka.actor.Actor
import akka.actor.Actor._
import akka.util.Logging

sealed trait Event
case class Connect(username: String) extends Event
case class Disconnect(username: String) extends Event


class Session extends Actor {
    def receive = {
        case "hello" => self reply "world"
        case _ => self reply "wtf?"
    }
}


object Client extends Logging {
    def main(args: Array[String]): Unit = {
        val actor = remote.actorFor("service:hello", "localhost", 5555)
        
        val result = actor !! "hello"
        log.info("result: %s", result)
        val res = actor !! "sup"
        log.info("res: %s", res)
    }    
}


object Server extends Logging {
    def main(args: Array[String]): Unit = {
        remote.start("localhost", 5555)
        // remote.registerPerSession("service:hello", actorOf[Session])
        // log.info(System.getProperty("akka.conf"))
        // log.info(System.getProp)
        
        // log.info(System.getProperty("java.class.path"))
    }
    
}