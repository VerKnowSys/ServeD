package com.verknowsys.served.utils

import scala.actors.Actor

/** 
 * Logger output class interface 
 * 
 * @author teamon
 */
abstract trait LoggerOutput {
    def log(msg: String, level: SvdLogger.Level.Value): Unit
}

/** 
 * Default logger output implementation 
 * 
 * @author teamon
 */
class ConsoleLoggerOutput extends LoggerOutput {
    def log(msg: String, level: SvdLogger.Level.Value){
        println(msg)
    }
}

/** 
 * Global logger 
 * 
 * @author teamon
 */
object SvdLogger extends Actor {
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
                case Log(owner, msg, level) => output.log(format % ("c" -> owner.getClass.getName, "m" -> msg), level)
                case _ =>
            }
        }
    }
}

class SvdLogger(owner: AnyRef){
    import SvdLogger.Log
    import SvdLogger.Level._
    
    def trace(msg: => String) = if(SvdLogger.level <= Trace) SvdLogger ! Log(owner, msg, Trace)
    def debug(msg: => String) = if(SvdLogger.level <= Debug) SvdLogger ! Log(owner, msg, Debug)
    def info(msg:  => String) = if(SvdLogger.level <= Info)  SvdLogger ! Log(owner, msg, Info)
    def warn(msg:  => String) = if(SvdLogger.level <= Warn)  SvdLogger ! Log(owner, msg, Warn)
    def error(msg: => String) = if(SvdLogger.level <= Error) SvdLogger ! Log(owner, msg, Error)
    
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
trait SvdLogged {
    lazy val logger = new SvdLogger(this)
}
