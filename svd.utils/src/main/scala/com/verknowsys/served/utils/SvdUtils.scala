package com.verknowsys.served.utils


import com.verknowsys.served._

import java.io.{PrintWriter, File, OutputStreamWriter}
import org.apache.commons.io.FileUtils
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

    
    /**
     *  @author dmilith
     *
     *  Get all live threads of ServeD. Useful only when debugging.
     *
     */
    def getAllLiveThreads = log.trace("Live threads list:\n%s".format(Thread.getAllStackTraces.toList.map{ th => "%s - %s\n".format(th._1, th._2.toList.map{ elem => "File name: %s, Class name: %s, Method name: %s, Line number: %d, (is Native? %b)\n".format(elem.getFileName, elem.getClassName, elem.getMethodName, elem.getLineNumber, elem.isNativeMethod)})}))
        

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
        
        
        
    /** 
     * Create new SvdLoopThread with provided function 
     * 
     * @author teamon
     */
    def loopThread(f: => Unit) = new SvdLoopThread(f)
    
    
    /** 
     * Checks if file exists
     * 
     * @author teamon
     */
    def fileExists(path: String) = (new java.io.File(path)).exists
    
    
    /** 
     * Removes directory
     * 
     * @author teamon
     */
    def rmdir(path: String) = try { FileUtils.forceDelete(path) } catch { case x: Throwable => log.warn(x.getMessage) }


    /** 
     * Changes owner of file at given path
     * 
     * @author dmilith
     */
    def chown(path: String, user: Int, group: Int = SvdConfig.defaultUserGroup, recursive: Boolean = true) = {
        // 2011-01-31 00:24:13 - dmilith - TODO: implement recursive call
        import CLibrary._
        val clib = CLibrary.instance
        if (clib.chown(path, user, group) == 0)
            0
        else
            -1
    }
    
    
    /** 
     * Changes permissions of file at given path
     * 
     * @author dmilith
     */
    def chmod(path: String, user: Int, group: Int = SvdConfig.defaultUserGroup, recursive: Boolean = true) = {
        // 2011-01-31 00:24:13 - dmilith - TODO: implement recursive call
        import CLibrary._
        val clib = CLibrary.instance
        if (clib.chmod(path, user) == 0)
            0
        else
            -1
    }
    
    
}

