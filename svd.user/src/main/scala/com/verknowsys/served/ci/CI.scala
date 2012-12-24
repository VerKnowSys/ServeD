package com.verknowsys.served.ci

import com.verknowsys.served.utils._

import akka.actor._


class CI extends SvdActor {

    def receive = {
        case Build =>
            log.trace("TestCI received Build")
            val tasks = Task.Clean :: Task.Update :: Task.Test :: Nil
            val worker = context.actorOf(Props(new Worker(tasks)))
            worker ! Build

        case BuildSucceed(history) =>
            log.debug("TestCI received BuildSucceed")
            // history.foreach(println)

        case BuildFailed(history) =>
            log.debug("TestCI received BuildFailed")
            // history.foreach(println)
    }
}
