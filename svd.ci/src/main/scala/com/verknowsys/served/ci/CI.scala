package com.verknowsys.served.ci

import akka.actor.Actor
import akka.actor.Actor.actorOf

class CI extends Actor {
    def receive = {
        case Build =>
            log.trace("TestCI received Build")
            val tasks = Task.Clean :: Task.Update :: Task.Test :: Nil
            val worker = actorOf(new Worker(tasks)).start
            worker ! Build
            
        case BuildSucceed(history) => 
            log.trace("TestCI received BuildSucceed")
            // history.foreach(println)
            
        case BuildFailed(history) =>
            log.trace("TestCI received BuildFailed")
            // history.foreach(println)
    }
}
