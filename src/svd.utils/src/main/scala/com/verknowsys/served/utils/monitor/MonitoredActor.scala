package com.verknowsys.served.utils.monitor

import scala.actors.Actor
import scala.collection.mutable.ListBuffer

object ActorsMonitor extends Actor {
    val actors = ListBuffer[MonitoredActor]()
    
    def register(actor: MonitoredActor){ actors += actor }
    
    def act {
        loop {
            actors foreach { actor => println(List(actor.hashCode(), actor.getClass.getName, actor.getState).mkString(",")) }
            Thread.sleep(400)
        }
    }
}

trait MonitoredActor extends Actor {
    override def start = {
        ActorsMonitor.register(this)
        super.start
    }
}