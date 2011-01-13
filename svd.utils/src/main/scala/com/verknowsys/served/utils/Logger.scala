package com.verknowsys.served.utils

import scala.actors.Actor

/** 
 * Logger output class interface 
 * 
 * @author teamon
 */
abstract trait LoggerOutput {
    def log(className: String, msg: String, level: Logger.Level.Value): Unit
}

/** 
 * Default logger output implementation 
 *
 * {{{
 * Logger.output = new ConsoleLoggerOutput("%{l} [%{c}]: %{m}") // set console output with custom format
 * 
 * #format parameters:
 *    %{c} - caller class name
 *    %{l} - message level
 *    %{m} - message content
 * }}}
 * 
 * @author teamon
 */
class ConsoleLoggerOutput(format: String) extends LoggerOutput {
    import ConsoleLoggerOutput._

    
    def this() = this(ConsoleLoggerOutput.DefaultFormat)
    
    def log(className: String, msg: String, level: Logger.Level.Value){
        val fmsg = format % ("l" -> level.toString, "c" -> className, "m" -> msg)
        println(Colors(level) + fmsg + DefaultColor)
    }
}

object ConsoleLoggerOutput {
    import Logger.Level._
    
    final val Colors = Map(
        Trace -> "\u001B[0;35m",
        Debug -> "\u001B[0;36m",
        Info -> "\u001B[0;37m",
        Warn -> "\u001B[1;33m",
        Error -> "\u001B[0;31m"
    )
    final val DefaultColor = "\u001B[0;39m"
    final val DefaultFormat = "%{l} [%{c}]: %{m}"
}

/** 
 * Global logger 
 * 
 * {{{
 * Logger.level = Debug                 // set logger level
 * }}}
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
    
    var level = Level.Trace
    var output: LoggerOutput = new ConsoleLoggerOutput

    start
        
    def act {
        loop {
            react {
                case Log(owner, msg, level) => output.log(owner.getClass.getName, msg, level)
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
