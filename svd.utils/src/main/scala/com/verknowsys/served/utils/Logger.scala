package com.verknowsys.served.utils

import scala.actors.Actor

/** 
 * Logger output class interface 
 * 
 * @author teamon
 */
abstract trait LoggerOutput {
    def log(msg: String, level: Logger.Level.Value): Unit
}

/** 
 * Default logger output implementation 
 * 
 * @author teamon
 */
class ConsoleLoggerOutput extends LoggerOutput {
    def log(msg: String, level: Logger.Level.Value){
        println(msg)
    }
}

/** 
 * Global logger 
 * 
 * @author teamon
 */
object Logger extends Actor {
    object Level extends Enumeration {
        val Trace,
            Debug,
            Info,
            Warn,
            Error = Value
    }
    
    case class Log(owner: AnyRef, msg: String, level: Level.Value)
    
    var level = Level.Debug
    var output: LoggerOutput = new ConsoleLoggerOutput
    var format = "[%{c}] %{m}"

    start
        
    def act {
        loop {
            react {
                case Log(owner, msg, level) => 
                    println("LOGGING: " + ("c" -> owner.getClass.getName, "m" -> msg))
                    output.log(format % ("c" -> owner.getClass.getName, "m" -> msg), level)
                case _ =>
            }
        }
    }
}

class Logger(owner: AnyRef){
    import Logger.Log
    import Logger.Level._
    
    def trace(msg: => String) = if(Logger.level <= Trace) Logger ! Log(owner, msg, Trace)
    def debug(msg: => String) = if(Logger.level <= Debug) Logger ! Log(owner, msg, Debug)
    def info(msg:  => String) = if(Logger.level <= Info)  Logger ! Log(owner, msg, Info)
    def warn(msg:  => String) = if(Logger.level <= Warn)  Logger ! Log(owner, msg, Warn)
    def error(msg: => String) = if(Logger.level <= Error) Logger ! Log(owner, msg, Error)
    
    def trace(x: Any): Unit = trace(x.toString)
    def debug(x: Any): Unit = debug(x.toString)
    def info(x: Any): Unit = info(x.toString)
    def warn(x: Any): Unit = warn(x.toString)
    def error(x: Any): Unit = error(x.toString)
}


/** 
 * Logger trait. Include it in you class to use logger 
 *
 * {{{
 * class A extends Logged {
 *     logger.debug("Debug message")
 * }
 * }}}
 * 
 * @author teamon
 */
trait Logged {
    lazy val logger = new Logger(this)
}
