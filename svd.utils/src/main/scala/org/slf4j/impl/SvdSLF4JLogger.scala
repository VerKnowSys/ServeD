// package org.slf4j.impl

// import org.slf4j.Logger
// import org.slf4j.Marker
// import org.slf4j.helpers.MessageFormatter
// import org.slf4j.helpers.NamedLoggerBase

// import com.verknowsys.served.utils.Logging
// import com.verknowsys.served.api.Logger.Levels._

// class SvdSLF4JLogger(val name: String) extends Logger with Logging {
//     def getName = name

//     def isTraceEnabled = true
//     def isDebugEnabled = true
//     def isInfoEnabled = true
//     def isWarnEnabled = true
//     def isErrorEnabled = true

//     def isTraceEnabled(marker: Marker) = true
//     def isDebugEnabled(marker: Marker) = true
//     def isInfoEnabled(marker: Marker) = true
//     def isWarnEnabled(marker: Marker) = true
//     def isErrorEnabled(marker: Marker) = true

//     def trace(marker: Marker, msg: String, t: Throwable) = log.log(Trace, msg + t.toString, name)
//     def debug(marker: Marker, msg: String, t: Throwable) = log.log(Debug, msg + t.toString, name)
//     def info(marker: Marker,  msg: String, t: Throwable) = log.log(Info,  msg + t.toString, name)
//     def warn(marker: Marker,  msg: String, t: Throwable) = log.log(Warn,  msg + t.toString, name)
//     def error(marker: Marker, msg: String, t: Throwable) = log.log(Error, msg + t.toString, name)

//     def trace(marker: Marker, msg: String, a: Array[Object]) = log.log(Trace, MessageFormatter.arrayFormat(msg, a), name)
//     def debug(marker: Marker, msg: String, a: Array[Object]) = log.log(Debug, MessageFormatter.arrayFormat(msg, a), name)
//     def info(marker: Marker, msg:  String, a: Array[Object]) = log.log(Info,  MessageFormatter.arrayFormat(msg, a), name)
//     def warn(marker: Marker, msg:  String, a: Array[Object]) = log.log(Warn,  MessageFormatter.arrayFormat(msg, a), name)
//     def error(marker: Marker, msg: String, a: Array[Object]) = log.log(Error, MessageFormatter.arrayFormat(msg, a), name)

//     def trace(marker: Marker, msg: String, x1: Any, x2: Any) = log.log(Trace, MessageFormatter.format(msg, x1, x2), name)
//     def debug(marker: Marker, msg: String, x1: Any, x2: Any) = log.log(Debug, MessageFormatter.format(msg, x1, x2), name)
//     def info(marker: Marker, msg:  String, x1: Any, x2: Any) = log.log(Info,  MessageFormatter.format(msg, x1, x2), name)
//     def warn(marker: Marker, msg:  String, x1: Any, x2: Any) = log.log(Warn,  MessageFormatter.format(msg, x1, x2), name)
//     def error(marker: Marker, msg: String, x1: Any, x2: Any) = log.log(Error, MessageFormatter.format(msg, x1, x2), name)

//     def trace(marker: Marker, msg: String, x1: Any) = log.log(Trace, MessageFormatter.format(msg, x1), name)
//     def debug(marker: Marker, msg: String, x1: Any) = log.log(Debug, MessageFormatter.format(msg, x1), name)
//     def info(marker: Marker, msg:  String, x1: Any) = log.log(Info,  MessageFormatter.format(msg, x1), name)
//     def warn(marker: Marker, msg:  String, x1: Any) = log.log(Warn,  MessageFormatter.format(msg, x1), name)
//     def error(marker: Marker, msg: String, x1: Any) = log.log(Error, MessageFormatter.format(msg, x1), name)

//     def trace(marker: Marker, msg: String) = log.log(Trace, msg, name)
//     def debug(marker: Marker, msg: String) = log.log(Debug, msg, name)
//     def info(marker: Marker, msg:  String) = log.log(Info,  msg, name)
//     def warn(marker: Marker, msg:  String) = log.log(Warn,  msg, name)
//     def error(marker: Marker, msg: String) = log.log(Error, msg, name)


//     def trace(msg: String, t: Throwable) = log.log(Trace, msg + t.toString, name)
//     def debug(msg: String, t: Throwable) = log.log(Debug, msg + t.toString, name)
//     def info(msg:  String, t: Throwable) = log.log(Info,  msg + t.toString, name)
//     def warn(msg:  String, t: Throwable) = log.log(Warn,  msg + t.toString, name)
//     def error(msg: String, t: Throwable) = log.log(Error, msg + t.toString, name)

//     def trace(msg: String, a: Array[Object]) = log.log(Trace, MessageFormatter.arrayFormat(msg, a), name)
//     def debug(msg: String, a: Array[Object]) = log.log(Debug, MessageFormatter.arrayFormat(msg, a), name)
//     def info(msg:  String, a: Array[Object]) = log.log(Info,  MessageFormatter.arrayFormat(msg, a), name)
//     def warn(msg:  String, a: Array[Object]) = log.log(Warn,  MessageFormatter.arrayFormat(msg, a), name)
//     def error(msg: String, a: Array[Object]) = log.log(Error, MessageFormatter.arrayFormat(msg, a), name)

//     def trace(msg: String, x1: Any, x2: Any) = log.log(Trace, MessageFormatter.format(msg, x1, x2), name)
//     def debug(msg: String, x1: Any, x2: Any) = log.log(Debug, MessageFormatter.format(msg, x1, x2), name)
//     def info(msg:  String, x1: Any, x2: Any) = log.log(Info,  MessageFormatter.format(msg, x1, x2), name)
//     def warn(msg:  String, x1: Any, x2: Any) = log.log(Warn,  MessageFormatter.format(msg, x1, x2), name)
//     def error(msg: String, x1: Any, x2: Any) = log.log(Error, MessageFormatter.format(msg, x1, x2), name)

//     def trace(msg: String, x1: Any) = log.log(Trace, MessageFormatter.format(msg, x1), name)
//     def debug(msg: String, x1: Any) = log.log(Debug, MessageFormatter.format(msg, x1), name)
//     def info(msg:  String, x1: Any) = log.log(Info,  MessageFormatter.format(msg, x1), name)
//     def warn(msg:  String, x1: Any) = log.log(Warn,  MessageFormatter.format(msg, x1), name)
//     def error(msg: String, x1: Any) = log.log(Error, MessageFormatter.format(msg, x1), name)

//     def trace(msg: String) = log.log(Trace, msg, name)
//     def debug(msg: String) = log.log(Debug, msg, name)
//     def info(msg:  String) = log.log(Info,  msg, name)
//     def warn(msg:  String) = log.log(Warn,  msg, name)
//     def error(msg: String) = log.log(Error, msg, name)
// }