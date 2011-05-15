package com.verknowsys.served.utils.monitor

// import com.verknowsys.served.utils.SvdUtils

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node
import com.verknowsys.served.utils._
import scala.collection.mutable.ListBuffer

/**
 * SvdMonitores registered actors state
 * 
 * @author teamon
 *  
 * @example
 *      class MyActor extends Actore with SvdMonitored 
 *      ActorsSvdMonitor.start
 *      val a = new MyActor // and it's already registered for watching
 * 
 */

case class SvdMonitoredData(val time: Long, val list: Map[String, Actor.State.Value])
case object GetSvdMonitoredData

object SvdMonitor extends Actor with SvdMonitoredLike { // 2011-01-30 13:16:18 - dmilith - XXX: with SvdExceptionHandler
    final val port = 8888
    val startTime = System.currentTimeMillis
    val monitored = new ListBuffer[SvdMonitoredLike]
    
    def registerSvdMonitored(monitor: SvdMonitoredLike){ monitored += monitor }
    
    RemoteActor.classLoader = getClass().getClassLoader()
    
    def act {
        alive(port)
        register('SvdMonitor, self)
        
        registerSvdMonitored(this)
        println("SvdMonitor started")
                
        loop {
            react {
                case GetSvdMonitoredData =>
                    if(!monitored.isEmpty) sender ! SvdMonitoredData(System.currentTimeMillis-startTime, monitored.foldLeft(Map[String, Actor.State.Value]()){ (m, e) => m + (e.toString -> e.state) })
                
                case _ => //messageNotRecognized(_)
            }
        }
    }
    
    override def toString = "SvdMonitor"
}

trait SvdMonitoredLike {
    self: Actor =>
    
    def state = getState
}

trait SvdMonitored extends SvdMonitoredLike {
    self: Actor =>
    
    SvdMonitor.registerSvdMonitored(this)
}