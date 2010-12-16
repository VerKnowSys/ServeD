package com.verknowsys.served.utils.monitor

import com.verknowsys.served.utils.Utils

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

case class MonitoredData(val time: Long, val list: Map[String, Actor.State.Value])
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
            println("in da loop")
            receive {
                case GetMonitoredData =>
                    sender ! MonitoredData(System.currentTimeMillis-startTime, monitored.foldLeft(Map[String, Actor.State.Value]()){ (m, e) => m + (e.toString -> e.state) })
                
                case _ => println("WTF?")
            }
        }
    }
}

object Test {    
    def main(args: Array[String]): Unit = {
        
        Monitor.start
        
            
        val a =  new Actor with Monitored {
            def act {
                loop {
                    println("a loop")
                    
                    receive {
                        case "exit" => exit
                        case i:Int =>  
                            Thread.sleep(i*10)
                            
                        case x: Any => println(x)//exit
                    }
                }
            }
            
            override def toString = "a"
        }
        
        
        val b = new Actor with Monitored {
            def act {
                loop {
                    println("b loop")
                    
                    receive {
                        case "exit" => exit
                        case i:Int => Thread.sleep(i*10)
                        case x: Any => println(x) //exit
                    }
                }
            }

            override def toString = "b"
        }
        
        val c = new Actor with Monitored {
            def act {
                var i = 0
                loop {
                    println("c loop")
                    
                    receiveWithin(500) {
                        case TIMEOUT => 
                            println("a")
                            i+=1
                            a ! i
                        case x: Any => println(x)//exit
                     }
                 }
            }

            override def toString = "c"
        }
        
        val d = new Actor with Monitored {
            def act {
                var i = 0
                loop {
                    println("d loop")
                    receiveWithin(130) {
                        case TIMEOUT =>
                            println("b")
                            i+=1
                            b ! i
                        case x: Any => println(x)//exit
                     }
                 }
            }

            override def toString = "d"
        }
        
        val e = new Actor with Monitored {
            def act { }     
            
            override def toString = "e"       
        }
                
        (a :: b :: c :: d :: Nil) foreach { _.start }
        
        println("Actors started")
    }
}

trait Monitored {
    self: Actor =>
    
    Monitor.registerMonitored(this)
    
    def state = getState
}