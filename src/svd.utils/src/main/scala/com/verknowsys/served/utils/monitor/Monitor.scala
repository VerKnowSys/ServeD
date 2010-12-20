package com.verknowsys.served.utils.monitor

// import com.verknowsys.served.utils.Utils

import scala.actors._
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

@serializable case class MonitoredData(val time: Long, val list: Map[String, Actor.State.Value])
@serializable case object GetMonitoredData

object Monitor extends Actor with MonitoredLike {
    final val port = 8888
    val startTime = System.currentTimeMillis
    val monitored = new ListBuffer[MonitoredLike]
    
    def registerMonitored(monitor: MonitoredLike){ monitored += monitor }
    
    RemoteActor.classLoader = getClass().getClassLoader()
    
    def act {
        alive(port)
        register('SvdMonitor, self)
        
        registerMonitored(this)
        println("Monitor started")
                
        loop {
            react {
                case GetMonitoredData =>
                    if(!monitored.isEmpty) sender ! MonitoredData(System.currentTimeMillis-startTime, monitored.foldLeft(Map[String, Actor.State.Value]()){ (m, e) => m + (e.toString -> e.state) })
                
                case _ => //messageNotRecognized(_)
            }
        }
    }
    
    override def toString = "Monitor"
}

trait MonitoredLike {
    self: Actor =>
    
    def state = getState
}

trait Monitored extends MonitoredLike {
    self: Actor =>
    
    Monitor.registerMonitored(this)
}