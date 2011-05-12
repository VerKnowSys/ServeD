package com.verknowsys.served.utils

import akka.actor.Actor
import akka.event.EventHandler

trait Logging {
    @transient lazy val log = Logger(this.getClass.getName)
}

// object Logger {
//     def apply(logger: String) = new Logger(logger)
// }


// Map(
//     "com.verknowsys.served.utils" -> Warn,
//     
// )

object Logger {
    object Level extends Enumeration {
        val Error, Warn, Info, Debug, Trace = Value
    }
    
    def apply(className: String): Logger = new ConsoleLogger(className)
    
    def levelFor(className: String): Level.Value = Level.Trace
}

// class LoggingManager extends Actor {
//     def receive = {
//         case LogLevel => self.reply
//     }
// }

class ConsoleLogger(klazz: String) extends Logger(klazz){
    protected[utils] def display(level: Logger.Level.Value, message: String, className: String) { 
        println(level + " : (" + className + ") " + message)
    }
}

abstract class Logger(klazz: String) {
    import Logger.Level._
    
    def error(msg: => String) = log(Error, msg)
    def error(msg: String, arg: Any, args: Any*): Unit = error(msg.format((arg :: args.toList):_*))
    
    def warn(msg: => String) = log(Warn, msg)
    def warn(msg: String, arg: Any, args: Any*): Unit = warn(msg.format((arg :: args.toList):_*))
    
    def info(msg: => String) = log(Info, msg)
    def info(msg: String, arg: Any, args: Any*): Unit = info(msg.format((arg :: args.toList):_*))
    
    def debug(msg: => String) = log(Debug, msg)
    def debug(msg: String, arg: Any, args: Any*): Unit = debug(msg.format((arg :: args.toList):_*))
    
    def trace(msg: => String) = log(Trace, msg)
    def trace(msg: String, arg: Any, args: Any*): Unit = trace(msg.format((arg :: args.toList):_*))
    
    protected[utils] def level = Logger.levelFor(klazz)
    
    protected[utils] def log(lvl: Logger.Level.Value, message: => String, className: String = klazz) = {
        if(level >= lvl) display(lvl, message, className)
    }

    protected[utils] def display(level: Logger.Level.Value, message: String, className: String)
}

class LoggingEventHandler extends Actor with Logging {
    import EventHandler._
    
    self.dispatcher = EventHandler.EventHandlerDispatcher
    
    def receive = {
        case Error(cause, instance, message) => 
            log.log(Logger.Level.Error, message.toString, instance.getClass.getName)
        
        case Warning(instance, message) =>  
            log.log(Logger.Level.Warn, message.toString, instance.getClass.getName)
        
        case Info(instance, message) =>
            log.log(Logger.Level.Info, message.toString, instance.getClass.getName)
            
        case Debug(instance, message) =>
            log.log(Logger.Level.Debug, message.toString, instance.getClass.getName)
            
        case event => 
            log.debug(event.toString)
    }
}
