package com.verknowsys.served.utils

import scala.actors.Actor

abstract trait LoggerOutput {
    def log(msg: String, level: SvdLogger.Level.Value): Unit
}

class ConsoleLoggerOutput extends LoggerOutput {
    def log(msg: String, level: SvdLogger.Level.Value){
        println(msg)
    }
}

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
    var format = "[%s] %s"

    start
        
    def act {
        loop {
            react {
                case Log(owner, msg, level) => output.log(format.format(owner, msg), level)
                case _ =>
            }
        }
    }
}

class SvdLogger(owner: AnyRef){
    import SvdLogger.Log
    import SvdLogger.Level._
    
    // def trace(msg: => String) = if(SvdLogger.level <= Trace) SvdLogger ! Log(owner, msg, Trace)
    def debug(msg: => String) = if(SvdLogger.level <= Debug) SvdLogger ! Log(owner, msg, Debug)
    def info(msg:  => String) = if(SvdLogger.level <= Info)  SvdLogger ! Log(owner, msg, Info)
    def warn(msg:  => String) = if(SvdLogger.level <= Warn)  SvdLogger ! Log(owner, msg, Warn)
    def error(msg: => String) = if(SvdLogger.level <= Error) SvdLogger ! Log(owner, msg, Error)
    
    
    def trace(msg: => String, args: Any*) = if(SvdLogger.level <= Trace) SvdLogger ! Log(owner, msg.format(args:_*), Trace)
}

trait SvdLogged {
    lazy val logger = new SvdLogger(this)
}
