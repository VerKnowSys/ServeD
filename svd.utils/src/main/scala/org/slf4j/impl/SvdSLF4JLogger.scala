package org.slf4j.impl

import org.slf4j.Logger
import org.slf4j.Marker

import com.verknowsys.served.utils.Logging
import com.verknowsys.served.api.Logger.Levels._

class SvdSLF4JLogger(val name: String) extends Logger with Logging {
    def getName = name

    def isTraceEnabled = true
    def isDebugEnabled = true
    def isInfoEnabled = true
    def isWarnEnabled = true
    def isErrorEnabled = true

    def isTraceEnabled(marker: Marker) = true
    def isDebugEnabled(marker: Marker) = true
    def isInfoEnabled(marker: Marker) = true
    def isWarnEnabled(marker: Marker) = true
    def isErrorEnabled(marker: Marker) = true

    def trace(marker: Marker, msg: String, t: Throwable) = log.log(Trace, msg + t.toString, name)
    def debug(marker: Marker, msg: String, t: Throwable) = log.log(Debug, msg + t.toString, name)
    def info(marker: Marker,  msg: String, t: Throwable) = log.log(Info,  msg + t.toString, name)
    def warn(marker: Marker,  msg: String, t: Throwable) = log.log(Warn,  msg + t.toString, name)
    def error(marker: Marker, msg: String, t: Throwable) = log.log(Error, msg + t.toString, name)

    def trace(marker: Marker, msg: String, a: Array[Object]) = log.log(Trace, msg, name)
    def debug(marker: Marker, msg: String, a: Array[Object]) = log.log(Debug, msg, name)
    def info(marker: Marker, msg:  String, a: Array[Object]) = log.log(Info,  msg, name)
    def warn(marker: Marker, msg:  String, a: Array[Object]) = log.log(Warn,  msg, name)
    def error(marker: Marker, msg: String, a: Array[Object]) = log.log(Error, msg, name)

    def trace(marker: Marker, msg: String, x1: Any, x2: Any) = log.log(Trace, msg, name)
    def debug(marker: Marker, msg: String, x1: Any, x2: Any) = log.log(Debug, msg, name)
    def info(marker: Marker, msg:  String, x1: Any, x2: Any) = log.log(Info,  msg, name)
    def warn(marker: Marker, msg:  String, x1: Any, x2: Any) = log.log(Warn,  msg, name)
    def error(marker: Marker, msg: String, x1: Any, x2: Any) = log.log(Error, msg, name)

    def trace(marker: Marker, msg: String, x1: Any) = log.log(Trace, msg, name)
    def debug(marker: Marker, msg: String, x1: Any) = log.log(Debug, msg, name)
    def info(marker: Marker, msg:  String, x1: Any) = log.log(Info,  msg, name)
    def warn(marker: Marker, msg:  String, x1: Any) = log.log(Warn,  msg, name)
    def error(marker: Marker, msg: String, x1: Any) = log.log(Error, msg, name)

    def trace(marker: Marker, msg: String) = log.log(Trace, msg, name)
    def debug(marker: Marker, msg: String) = log.log(Debug, msg, name)
    def info(marker: Marker, msg:  String) = log.log(Info,  msg, name)
    def warn(marker: Marker, msg:  String) = log.log(Warn,  msg, name)
    def error(marker: Marker, msg: String) = log.log(Error, msg, name)


    def trace(msg: String, t: Throwable) = log.log(Trace, msg + t.toString, name)
    def debug(msg: String, t: Throwable) = log.log(Debug, msg + t.toString, name)
    def info(msg:  String, t: Throwable) = log.log(Info,  msg + t.toString, name)
    def warn(msg:  String, t: Throwable) = log.log(Warn,  msg + t.toString, name)
    def error(msg: String, t: Throwable) = log.log(Error, msg + t.toString, name)

    def trace(msg: String, a: Array[Object]) = log.log(Trace, msg + a.mkString(", "), name)
    def debug(msg: String, a: Array[Object]) = log.log(Debug, msg + a.mkString(", "), name)
    def info(msg:  String, a: Array[Object]) = log.log(Info,  msg + a.mkString(", "), name)
    def warn(msg:  String, a: Array[Object]) = log.log(Warn,  msg + a.mkString(", "), name)
    def error(msg: String, a: Array[Object]) = log.log(Error, msg + a.mkString(", "), name)

    def trace(msg: String, x1: Any, x2: Any) = log.log(Trace, msg + x1.toString + x2.toString, name)
    def debug(msg: String, x1: Any, x2: Any) = log.log(Debug, msg + x1.toString + x2.toString, name)
    def info(msg:  String, x1: Any, x2: Any) = log.log(Info,  msg + x1.toString + x2.toString, name)
    def warn(msg:  String, x1: Any, x2: Any) = log.log(Warn,  msg + x1.toString + x2.toString, name)
    def error(msg: String, x1: Any, x2: Any) = log.log(Error, msg + x1.toString + x2.toString, name)

    def trace(msg: String, x1: Any) = log.log(Trace, msg + x1.toString, name)
    def debug(msg: String, x1: Any) = log.log(Debug, msg + x1.toString, name)
    def info(msg:  String, x1: Any) = log.log(Info,  msg + x1.toString, name)
    def warn(msg:  String, x1: Any) = log.log(Warn,  msg + x1.toString, name)
    def error(msg: String, x1: Any) = log.log(Error, msg + x1.toString, name)

    def trace(msg: String) = log.log(Trace, msg, name)
    def debug(msg: String) = log.log(Debug, msg, name)
    def info(msg:  String) = log.log(Info,  msg, name)
    def warn(msg:  String) = log.log(Warn,  msg, name)
    def error(msg: String) = log.log(Error, msg, name)
}
