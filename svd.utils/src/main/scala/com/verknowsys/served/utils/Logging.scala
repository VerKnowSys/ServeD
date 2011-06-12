package com.verknowsys.served.utils

import akka.actor.Actor
import akka.event.EventHandler
import java.io.FileWriter

import com.verknowsys.served.SvdConfig
import com.verknowsys.served.api._


trait Logging {
    @transient lazy val log = new ConsoleLogger(this.getClass.getName)
}


object LoggerUtils {
    protected var _levels = readConfig
    
    def levels = _levels
    
    def levelFor(className: String) = _level(className.split("\\.").reverse.toList) getOrElse Logger.Levels.Trace
    
    def loggerConfig = new SvdProperties(SvdConfig.systemTmpDir / "svd.logger")
    
    def readConfig = loggerConfig.data.map(_.mapValues { e => e.toLowerCase match {
        case "error" => Logger.Levels.Error
        case "warn"  => Logger.Levels.Warn
        case "info"  => Logger.Levels.Info
        case "debug" => Logger.Levels.Debug
        case "trace" => Logger.Levels.Trace
        case _ => Logger.Levels.Debug
    } }.toMap) getOrElse Map()
    
    def update {
        _levels = readConfig
    }
    
    def addEntry(className: String, level: Logger.Levels.Value){
        loggerConfig(className) = level.toString
        update
    }
    
    def removeEntry(className: String){
        loggerConfig.remove(className)
        update
    }
    
    // Serach for matching class entry
    protected def _level(parts: List[String]): Option[Logger.Levels.Value] = parts match {
        case x :: xs => _levels.get(parts.reverse.mkString(".")) orElse _level(xs)
        case Nil => None
    }
}


class ConsoleLogger(klazz: String) extends AbstractLogger(klazz){
    import Logger.Levels._
    
    final val Colors = Map(
        Error -> Console.RED,
        Warn  -> Console.YELLOW,
        Info  -> Console.WHITE,
        Debug -> Console.CYAN,
        Trace -> Console.MAGENTA
    )
    
    protected[utils] def display(level: Logger.Levels.Value, message: String, className: String) {
        import java.text.SimpleDateFormat
        val fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dte = new java.util.Date
        message.split("\n").foreach(line =>
            println("%s%s%s <%s%s%s> [%s%s%s]".format(
                Colors(level), fmt.format(dte), Console.RESET,
                Colors(level), formatClassName(className), Console.RESET,
                Colors(level), line, Console.RESET)
            )
        )
    }
    
    protected[utils] def formatClassName(className: String) = className.replace("com.verknowsys.served", "svd")
}


abstract class AbstractLogger(klazz: String) {
    import Logger.Levels._
    
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
    
    protected[utils] def log(lvl: Logger.Levels.Value, message: => String, className: String = klazz) = {
        if(LoggerUtils.levelFor(className) >= lvl) display(lvl, message, className)
    }

    protected[utils] def display(level: Logger.Levels.Value, message: String, className: String)
}

class LoggingEventHandler extends Actor with Logging {
    import Logger.Levels._
    
    self.dispatcher = EventHandler.EventHandlerDispatcher
    
    def receive = {
        case EventHandler.Error(cause, instance, message) => 
            log.log(Error, message.toString, instance.getClass.getName)
        
        case EventHandler.Warning(instance, message) =>  
            log.log(Warn, message.toString, instance.getClass.getName)
        
        case EventHandler.Info(instance, message) =>
            log.log(Info, message.toString, instance.getClass.getName)
            
        case EventHandler.Debug(instance, message) =>
            log.log(Debug, message.toString, instance.getClass.getName)
            
        case event => 
            log.debug(event.toString)
    }
}
