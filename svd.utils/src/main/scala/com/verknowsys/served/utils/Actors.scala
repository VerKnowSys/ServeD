package com.verknowsys.served.utils



/** 
 * Abstract class. To be used as base for most of actors 
 * 
 * @author teamon
 */
abstract class CommonActor extends ExceptionHandlingActor with Logged {
    
    def messageNotRecognized(x: Any) {
        logger.warn("Message not recognized: " + x.toString)
    }
}

/** 
 * Handle exceptions thrown by actor 
 * 
 * @author teamon
 */
abstract class ExceptionHandlingActor extends Actor {
    
    override def react(handler: PartialFunction[Any, Unit]) = super.react(handleExceptions(handler))
    
    override def reactWithin(msec: Long)(handler: PartialFunction[Any, Unit]) = super.reactWithin(msec)(handleExceptions(handler))
    
    override def receive[R](f: PartialFunction[Any, R]) = super.receive(handleExceptions(f))
    
    override def receiveWithin[R](msec: Long)(f: PartialFunction[Any, R]) = super.receiveWithin(msec)(handleExceptions(f))
    
    private def handleExceptions[R](handler: PartialFunction[Any, R]): PartialFunction[Any, R] = {
        case x =>
            try {
                handler(x)
                null.asInstanceOf[R] // HACK: Just for TypeChecker                
            } catch {
                case e => 
                    println(this + " got exception " + e)
                    null.asInstanceOf[R] // HACK: Just for TypeChecker
            }
    }
}
