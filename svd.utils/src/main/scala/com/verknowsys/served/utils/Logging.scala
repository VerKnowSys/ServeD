package com.verknowsys.served.utils

import akka.actor.{Actor, ActorRef}
// import akka.event.EventHandler
import akka.event.{Logging => AkkaLogging}

import com.verknowsys.served.api._


trait Logging {
    @transient lazy val log = new ConsoleLogger(this.getClass.getName)
}

object GlobalLogger extends LoggingMachine

trait LoggingMachine {
    // read defaults from properteis file
    def readDefaults = {
        Option(getClass.getResource("/svd.logger.properties")) flatMap { res =>
            val props = new SvdProperties(res.getPath)
            props.data.map { pairs =>
                (Map[String, Logger.Levels.Value]() /: pairs) {
                    case(config, (className, str)) => levelsMapping.get(str.toLowerCase) match {
                        case Some(level) => config + (className -> level)
                        case None => config
                    }
                }
            }
        } getOrElse Map[String, Logger.Levels.Value]()
    }

    val levelsMapping = Map(
        "trace" -> Logger.Levels.Trace,
        "debug" -> Logger.Levels.Debug,
        "info"  -> Logger.Levels.Info,
        "warn"  -> Logger.Levels.Warn,
        "error" -> Logger.Levels.Error
    )

    protected var config = readDefaults

    def levels = config.toMap

    def levelFor(className: String) = _level(className.split("\\.").reverse.toList) getOrElse Logger.Levels.Trace

    def addEntry(className: String, level: Logger.Levels.Value){
        config = config + (className -> level)
    }

    def removeEntry(className: String){
        config = config - className
    }

    def clear {
        config = Map[String, Logger.Levels.Value]()
    }

    // Serach for matching class entry
    protected def _level(parts: List[String]): Option[Logger.Levels.Value] = parts match {
        case x :: xs => config.get(parts.reverse.mkString(".")) orElse _level(xs)
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

    /*protected[utils] */ def log(lvl: Logger.Levels.Value, message: => String, className: String = klazz) = {
        if(GlobalLogger.levelFor(className) >= lvl) display(lvl, message, className)
    }

    protected[utils] def display(level: Logger.Levels.Value, message: String, className: String)
}

class LoggingEventHandler extends Actor with Logging {
    import Logger.Levels._

    // self.dispatcher = EventHandlerDispatcher

    def receive = {
        case AkkaLogging.Error(cause, "Error", instance, message) =>
            log.log(Error, message.toString, resolveClassName(instance))
            log.log(Error, cause.toString, resolveClassName(instance))
            cause.printStackTrace(java.lang.System.out)

        case AkkaLogging.Warning("Warning", instance, message) =>
            log.log(Warn, message.toString, resolveClassName(instance.getClass.getName))

        case AkkaLogging.Info("Info", instance, message) =>
            log.log(Info, message.toString, resolveClassName(instance.getClass.getName))

        case AkkaLogging.Debug("Debug", instance, message) =>
            log.log(Debug, message.toString, resolveClassName(instance.getClass.getName))

        case event =>
            log.debug(event.toString)
    }

    def resolveClassName(x: AnyRef) = {
        val name = x match {
            case ar: ActorRef => ar.getClass.getName //actorClassName
            case _ => x.getClass.getName
        }
        name + "(akka)"
    }
}
