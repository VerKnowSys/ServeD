package com.verknowsys.served.utils

import scala.actors.Actor

/** 
 * Abstract class. To be used as base for most of actors 
 * 
 * @author teamon
 */
abstract class CommonActor extends Actor with Logged {
    
    def messageNotRecognized(x: Any) {
        warn("Message not recognized: " + x.toString)
    }
    
    override def exceptionHandler: PartialFunction[Exception, Unit] = {
        case e => println(this + " got exception " + e) // Logger ! Exception(this, e)
    }
}
