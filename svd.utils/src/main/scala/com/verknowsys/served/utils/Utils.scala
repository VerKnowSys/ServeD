// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils

import com.verknowsys.served._
import com.verknowsys.served.utils.kqueue.Kqueue
import java.io.{PrintWriter, File, OutputStreamWriter}
import java.util.ArrayList
import java.util.regex.Pattern
import org.apache.log4j._
import clime.messadmin.providers.sizeof.ObjectProfiler



/**
*   @author dmilith
*   
*   Utils trait should be used by every Actor in ServeD
*   
*/
trait Utils extends UtilsCommon {
    
    // checkOrCreateVendorDir 
    // XXX: This makes unnecessary logger and I/O bloat.
    //      No need to run this for every class using Utils trait. Should be moved somewhere else
    
        
    /**
    *   @author dmilith
    *   
    *   Get and spread props for all believers..
    *   
    */   
    lazy val props = Config.props
    
    lazy val logger = {
        BasicConfigurator.resetConfiguration
        reloadLoggerConfiguration
        Logger.getLogger(this.getClass)
    }
    
    val loggerPropertiesFileWatch = Kqueue.watch(Config.loggerConfigFile, modified = true) { 
        logger.debug("Logger properties file changed: " + Config.loggerConfigFile)
        reloadLoggerConfiguration 
    }
    addShutdownHook { loggerPropertiesFileWatch.stop }
    
    def reloadLoggerConfiguration {
        try { PropertyConfigurator.configure(Config.loggerConfigFile) } 
        catch { case _ => logger.error("Couldn`t load file %s".format(Config.loggerConfigFile)) }
    }
    

    /**
     *  @author dmilith
     *
     *  Checks and creates (if missing) config property file for application
     *
     */
    def checkOrCreateVendorDir = {
        val vendorPath = Config.homePath + Config.vendorDir
        if (new File(vendorPath).exists) {
            logger.debug("Making sure that vendor directory exists")
        } else {
            logger.debug("No vendor directory available! Creating empty vendor directory")
            new File(vendorPath).mkdir
        }
        vendorPath
    }
    
    
    def messageNotRecognized(x: Any) {
        logger.warn("Message not recognized: " + x.toString)
    }


    def getAllLT =
        logger.trace("Live threads list:\n%s".format(Thread.getAllStackTraces.toList.map{ th => "%s - %s\n".format(th._1, th._2.toList.map{ elem => "File name: %s, Class name: %s, Method name: %s, Line number: %d, (is Native? %b)\n".format(elem.getFileName, elem.getClassName, elem.getMethodName, elem.getLineNumber, elem.isNativeMethod)})}))
}




/**
*   @author dmilith
*   
*   UtilsCommon trait is higher level of abstraction (to be usable in tests).
*   Utils trait will always include UtilsCommon
*   
*/
trait UtilsCommon {


    /**
     *  @author dmilith
     *
     *  Adds a hook for SIGINT/ SIGTERM signals to gracefully close the app
     *
     *  @example
     *  addShutdownHook {
     *     closeMySockets
     *     logger.info("Dying!")
     *  }
     *
     */
    def addShutdownHook(block: => Unit) =
        Runtime.getRuntime.addShutdownHook(
            new Thread {
                override def run = block
            }
        )


    /**
     *  @author dmilith
     *
     *  Returns size (in bytes) of given object in JVM memory
     *
     *  @example
     *  logger.info ( sizeof ( new Date ( ) ))
     *
     */
    def sizeof(any: Any) = ObjectProfiler.sizeof(any)

    
    
}
