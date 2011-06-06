package com.verknowsys.served.systemmanager.managers

import akka.actor.Actor
import akka.event.EventHandler

import com.verknowsys.served.api.{Success, Logger}
import com.verknowsys.served.utils._


class LoggingManager extends Actor with Logging with SvdFileEventsReactor {
    log.info("Starting LoggingManager")
    
    def receive = {
        case Logger.ListEntries =>
            self reply Logger.Entries(LoggerUtils.levels)
            
        case Logger.AddEntry(className, level) =>
            log.trace("Setting logger level %s for class %s", level, className)
            LoggerUtils.addEntry(className, level)
            self reply Success
            
        case Logger.RemoveEntry(className) =>
            log.trace("Removing logger level settings for class %s", className)
            LoggerUtils.removeEntry(className)
            self reply Success
            
        // case events.SvdFileEvent(path, flags) =>
            // LoggerUtils.update
    }
    
    // override def preStart {
        // registerFileEventFor(SvdConfig.loggerPropertiesFilename, Modified)
    // }
}
