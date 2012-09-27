package com.verknowsys.served.managers


import com.verknowsys.served.api.{Success, Logger}
import com.verknowsys.served.utils._ //{LoggingMachine, GlobalActorRef, GlobalLogger}
import akka.actor._


// object LoggingManager //extends GlobalActorRef(actorOf(new LoggingManager(GlobalLogger)))
// XXX: CHECKME

class LoggingManager(val loggingMachine: LoggingMachine) extends SvdManager {
    log.info("Starting LoggingManager")

    def receive = {
        case Logger.ListEntries =>
            sender ! Logger.Entries(loggingMachine.levels)

        case Logger.AddEntry(className, level) =>
            log.trace("Setting logger level %s for class %s", level, className)
            loggingMachine.addEntry(className, level)
            sender ! Success

        case Logger.RemoveEntry(className) =>
            log.trace("Removing logger level settings for class %s", className)
            loggingMachine.removeEntry(className)
            sender ! Success

        // case events.SvdFileEvent(path, flags) =>
            // LoggerUtils.update
    }

    // override def preStart {
        // registerFileEventFor(SvdConfig.loggerPropertiesFilename, Modified)
    // }
}
