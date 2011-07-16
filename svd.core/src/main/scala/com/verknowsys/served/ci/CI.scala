package com.verknowsys.served.ci

import com.verknowsys.served.utils.Logging
import com.verknowsys.served.utils._

import akka.actor.Actor
import akka.actor.Actor.actorOf


class CI extends SvdExceptionHandler {
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
