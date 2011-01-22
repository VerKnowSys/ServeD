package com.verknowsys.served.utils

// This stuff will ALL be removed soon, stop using it

@deprecated("Just dont use it. Use akka.actor.Actor + akka.util.Logging instead")
abstract class CommonActor extends scala.actors.Actor with Logged {
    def messageNotRecognized(x: Any) {}
}

@deprecated("Use akka.util.Logging")
trait Logged extends akka.util.Logging {
    def trace(x: Any): Unit = log.trace(x.toString)
    def debug(x: Any): Unit = log.debug(x.toString)
    def info(x: Any): Unit = log.info(x.toString)
    def warn(x: Any): Unit = log.warn(x.toString)
    def error(x: Any): Unit = log.error(x.toString)
}
