package com.verknowsys.served.utils

import com.verknowsys.served._
import java.io.{PrintWriter, File, OutputStreamWriter}
import java.util.ArrayList
import java.util.regex.Pattern
import clime.messadmin.providers.sizeof.ObjectProfiler
import scala.collection.JavaConversions._

import akka.util.Logging

/**
 * SvdUtils object containing common functions
 * 
 * @author dmilith
 *   
 */
object SvdUtils extends Logging {
    
    // checkOrCreateVendorDir 
    // XXX: This makes unnecessary logger and I/O bloat.
    //      No need to run this for every class using SvdUtils trait. Should be moved somewhere else
    
        
    /**
    *   @author dmilith
    *   
    *   Get and spread props for all believers..
    *   
    */   
    lazy val props = SvdConfig.props
    
    
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
    //     BasicSvdConfigurator.resetSvdConfiguration
    //     reloadLoggerSvdConfiguration
    //     Logger.getLogger(this.getClass)
    // }
    
    // val loggerPropertiesFileWatch = SvdKqueue.watch(SvdConfig.loggerSvdConfigFile, modified = true) { 
    //     debug("Logger properties file changed: " + SvdConfig.loggerSvdConfigFile)
    //     reloadLoggerSvdConfiguration 
    // }
    // addShutdownHook { loggerPropertiesFileWatch.stop }
    
    // def reloadLoggerSvdConfiguration {
    //     try { PropertySvdConfigurator.configure(SvdConfig.loggerSvdConfigFile) } 
    //     catch { case _ => error("Couldn`t load file %s".format(SvdConfig.loggerSvdConfigFile)) }
    // }
    

    /**
     *  @author dmilith
     *
     *  Checks and creates (if missing) config property file for application
     *
     */
    def checkOrCreateVendorDir = {
        val vendorPath = SvdConfig.homePath + SvdConfig.vendorDir
        if (new File(vendorPath).exists) {
            log.debug("Making sure that vendor directory exists")
        } else {
            log.debug("No vendor directory available! Creating empty vendor directory")
            new File(vendorPath).mkdir
        }
        vendorPath
    }
    
    def getAllLT = log.trace("Live threads list:\n%s".format(Thread.getAllStackTraces.toList.map{ th => "%s - %s\n".format(th._1, th._2.toList.map{ elem => "File name: %s, Class name: %s, Method name: %s, Line number: %d, (is Native? %b)\n".format(elem.getFileName, elem.getClassName, elem.getMethodName, elem.getLineNumber, elem.isNativeMethod)})}))
        
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
