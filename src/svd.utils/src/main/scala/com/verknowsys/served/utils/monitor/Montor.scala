package com.verknowsys.served.utils.monitor

import com.verknowsys.served.utils.Utils

import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node
import scala.collection.mutable.ListBuffer

/**
 * Monitores registered actors state
 * 
 * @author teamon
 *  
 * @example
 *      class MyActor extends Actore with Monitored
 *      ActorsMonitor.start
 *      val a = new MyActor // and it's already registered for watching
 * 
 */

case class MonitoredData(val time: Long, val list: List[(String, Actor.State.Value)])
case object GetMonitoredData

object Monitor extends Actor with Utils {
    final val port = 8888
    val startTime = System.currentTimeMillis
    val monitored = new ListBuffer[Monitored]
    
    def registerMonitored(monitor: Monitored){ monitored += monitor }
    
    RemoteActor.classLoader = getClass().getClassLoader()
    
    def act {
        alive(port)
        register('SvdMonitor, self)
        
        logger.info("Monitor started")
                
        loop {
            react {
                case GetMonitoredData =>
                    sender ! MonitoredData(System.currentTimeMillis-startTime, monitored.toList.map { m => 
                        (m.toString, m.state) 
                    })
                
                case _ => println("WTF?")
            }
        }
    }
}

trait Monitored {
    self: Actor =>
    
    Monitor.registerMonitored(this)
    
    def state = getState
}