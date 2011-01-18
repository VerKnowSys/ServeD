package com.verknowsys.served.playground

import akka.actor._
import akka.actor.Actor._
import akka.config.Supervision._
import akka.util.Logging
import com.verknowsys.served.utils.Logged

class Foo extends Exception


object Server {
    
    class Master extends Actor with Logged {
        self.spawnLink[A]
        self.spawnLink[A]
        self.spawnLink[A]
        self.spawnLink[A]
        
        
        def receive = {
            case "die" =>
                trace("Master die!")
                throw new Foo
        }
    }
    
    class A extends Actor with Logged {
        override def preRestart(reason: Throwable) {
            trace(this + " preRestart | " + reason)
        }
        
        override def postRestart(reason: Throwable) {
            trace(this + "A postRestart | " + reason)
        }
        
        override def preStart {
            trace(this + "A preInit")
        }
        
        def receive = {
            case "die" => 
                trace("A got die :(")
                throw new Foo
            case x => trace("A got " + x)
        }
    }
    
    
    
    def main(args: Array[String]): Unit = {

        val master = actorOf[Master]
        
        Supervisor(
            SupervisorConfig(
                OneForOneStrategy(List(classOf[Foo]), 3, 1000),
                Supervise(master, Permanent) :: Nil
            )
        )
        
        Thread.sleep(5000)
        master ! "die"
    }
}