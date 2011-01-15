package com.verknowsys.served.utils

import scala.actors.Actor
import com.verknowsys.served.utils.kqueue.Kqueue
import com.verknowsys.served.Config


/** 
 * Logger output class interface 
 * 
 * @author teamon
 */
abstract trait LoggerOutput {
    def log(sender: AnyRef, msg: Logger.Message): Unit
}

/** 
 * Default logger output implementation 
 *
 * {{{
 * format parameters:
 *    %{c} - caller class name
 *    %{l} - message level
 *    %{m} - message content
 * }}}
 * 
 * @author teamon
 */
class ConsoleLoggerOutput extends LoggerOutput {
    import ConsoleLoggerOutput._
    
    def log(sender: AnyRef, msg: Logger.Message){
        val fmsg = format % ("l" -> msg.level.toString, "c" -> msg.caller.toString, "m" -> msg.content)
        println(Colors(msg.level) + fmsg + DefaultColor)
    }
    
    private var format = DefaultFormat // XXX: Var
        
    val propertiesFileWatch = Kqueue.watch(Config.loggerConfigFile, modified = true) { 
        reloadConfiguration
    }
    
    addShutdownHook { propertiesFileWatch.stop }
    
    reloadConfiguration
    
    private def reloadConfiguration {
        println("=== Modified logger.properties")
        
        synchronized {
            val props = new Properties(Config.loggerConfigFile)
            format = props("logger.console.format") or DefaultFormat
        }
    }
}

object ConsoleLoggerOutput extends UtilsCommon {
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
object Logger extends Actor with UtilsCommon {
    object Level extends Enumeration {
        val Trace,
            Debug,
            Info,
            Warn,
            Error = Value
    }
    
    case class Message(caller: AnyRef, content: String, level: Level.Value)
    
    @volatile private var _level = Level.Trace
    @volatile private var _output: LoggerOutput = new ConsoleLoggerOutput
    
    def level = _level
    
    def level_=(lvl: Level.Value) = synchronized {
        _level = lvl
    }
    
    def output = _output
    
    def output_=(out: LoggerOutput) = synchronized {
        _output = out
    }

    start
        
    def act {
        loop {
            react {
                case msg @ Message(owner, content, level) => output.log(sender, msg)
                case _ =>
            }
        }
    }
    
    val propertiesFileWatch = Kqueue.watch(Config.loggerConfigFile, modified = true) { 
        reloadConfiguration
    }
    
    addShutdownHook { propertiesFileWatch.stop }
    
    reloadConfiguration
    
    private def reloadConfiguration {
        println("=== Modified logger.properties")
        val props = new Properties(Config.loggerConfigFile)
        level = findLevel(props("logger.level") or "trace") getOrElse Level.Trace
    }
        
    private def findLevel(s: String) = Map(
        "trace" -> Level.Trace,
        "debug" -> Level.Debug,
        "info" -> Level.Info,
        "warn" -> Level.Warn,
        "error" -> Level.Error
    ).get(s.toLowerCase)
        
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
    @deprecated("Use just 'trace' instead of 'logger.trace' (and other methods as well)")
    lazy val logger = this
    
    import Logger.Message
    import Logger.Level._
    
    def trace(msg: => String) = if(Logger.level <= Trace) Logger ! Message(this, msg, Trace)
    def debug(msg: => String) = if(Logger.level <= Debug) Logger ! Message(this, msg, Debug)
    def info(msg:  => String) = if(Logger.level <= Info)  Logger ! Message(this, msg, Info)
    def warn(msg:  => String) = if(Logger.level <= Warn)  Logger ! Message(this, msg, Warn)
    def error(msg: => String) = if(Logger.level <= Error) Logger ! Message(this, msg, Error)
    
    def trace(x: Any): Unit = trace(x.toString)
    def debug(x: Any): Unit = debug(x.toString)
    def info(x: Any): Unit = info(x.toString)
    def warn(x: Any): Unit = warn(x.toString)
    def error(x: Any): Unit = error(x.toString)
}
