package com.verknowsys.served.utils

import com.verknowsys.served._
import com.verknowsys.served.utils.kqueue.Kqueue
import java.io.{PrintWriter, File, OutputStreamWriter}
import java.util.ArrayList
import java.util.regex.Pattern
import clime.messadmin.providers.sizeof.ObjectProfiler
import scala.collection.JavaConversions._


/**
 * Utils object containing common functions
 * 
 * @author dmilith
 *   
 */
object Utils extends Logged {
    
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
    
    
    /**
    *   @author dmilith
    *   
    *   check and inform when current user isn't superuser (root)
    *   
    */   
    def rootCheck {
        System.getProperty("user.name") match {
            case "root" =>
            case _ =>
                lazy val err = "%s must be run as root user to perform some operations!".format(this.getClass)
                error(err)
        }
    }
    
    
    // lazy val logger = {
    //     BasicConfigurator.resetConfiguration
    //     reloadLoggerConfiguration
    //     Logger.getLogger(this.getClass)
    // }
    
    // val loggerPropertiesFileWatch = Kqueue.watch(Config.loggerConfigFile, modified = true) { 
    //     debug("Logger properties file changed: " + Config.loggerConfigFile)
    //     reloadLoggerConfiguration 
    // }
    // addShutdownHook { loggerPropertiesFileWatch.stop }
    
    // def reloadLoggerConfiguration {
    //     try { PropertyConfigurator.configure(Config.loggerConfigFile) } 
    //     catch { case _ => error("Couldn`t load file %s".format(Config.loggerConfigFile)) }
    // }
    

    /**
     *  @author dmilith
     *
     *  Checks and creates (if missing) config property file for application
     *
     */
    def checkOrCreateVendorDir = {
        val vendorPath = Config.homePath + Config.vendorDir
        if (new File(vendorPath).exists) {
            debug("Making sure that vendor directory exists")
        } else {
            debug("No vendor directory available! Creating empty vendor directory")
            new File(vendorPath).mkdir
        }
        vendorPath
    }
    
    def getAllLT = trace("Live threads list:\n%s".format(Thread.getAllStackTraces.toList.map{ th => "%s - %s\n".format(th._1, th._2.toList.map{ elem => "File name: %s, Class name: %s, Method name: %s, Line number: %d, (is Native? %b)\n".format(elem.getFileName, elem.getClassName, elem.getMethodName, elem.getLineNumber, elem.isNativeMethod)})}))
        
    /**
     *  @author dmilith
     *
     *  Returns size (in bytes) of given object in JVM memory
     *
     *  @example
     *  info ( sizeof ( new Date ( ) ))
     *
     */
    def sizeof(any: Any) = ObjectProfiler.sizeof(any)
    
    /**
     *  @author dmilith
     *
     *  Adds a hook for SIGINT/ SIGTERM signals to gracefully close the app
     *
     *  @example
     *  addShutdownHook {
     *     closeMySockets
     *     info("Dying!")
     *  }
     *
     */
    def addShutdownHook(block: => Unit) =
        Runtime.getRuntime.addShutdownHook(
            new Thread {
                override def run = block
            }
        )
}

