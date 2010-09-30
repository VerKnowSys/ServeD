// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils


import com.verknowsys.served._

import java.io.{PrintWriter, File, OutputStreamWriter}
import java.util.ArrayList
import java.util.regex.Pattern
import org.apache.log4j._
import clime.messadmin.providers.sizeof.ObjectProfiler

/**
 * User: dmilith
 * Date: Jul 10, 2009
 * Time: 4:28:05 PM
 */

trait Utils {
    lazy val loggerAppender = new ConsoleAppender
    lazy val logger = {
        loggerAppender.setName(ConsoleAppender.SYSTEM_OUT);
        loggerAppender.setWriter(new OutputStreamWriter(System.out))
        loggerAppender.setThreshold(if (Config.debug) Level.DEBUG else Level.INFO)
        loggerAppender.setLayout(new ANSIColorLayout("{ %-5p: [%c]: %m }%n"))
        if (Logger.getRootLogger.getAppender(ConsoleAppender.SYSTEM_OUT) == null) {
            Logger.getRootLogger.addAppender(loggerAppender)
        }
        Logger.getLogger(this.getClass)
    }
    checkOrCreateVendorDir
    lazy val mainConfigFile = Config.home + Config.vendorDir + Config.propertiesFile
    lazy val props = new Properties(mainConfigFile)

    /**
     * @author teamon
     *
     * Executes passed function only in debug mode
     *
     */
    def debug(f: => Unit) = if (Config.debug) f


    /**
     * @author dmilith
     *
     * Checks and creates (if missing) config property file for application
     *
     */
    def checkOrCreateVendorDir = {
        if (new File(Config.home + Config.vendorDir).exists) {
            logger.debug("Making sure that vendor directory exists…")
        } else {
            logger.debug("No vendor directory available! Creating empty vendor directory…")
            new File(Config.home + Config.vendorDir).mkdir
        }
        Config.home + Config.vendorDir + Config.propertiesFile
    }


    /**
     * @author dmilith
     *
     * Setting threshold of logger.
     * Default value is Level.INFO
     *
     * @example
     * threshold ( Level.DEBUG )
     *
     */
    def threshold(level: Level) = {
        logger // initialize cause it's lazy       \ debug
        loggerAppender.setThreshold(level) //       / purpose
    }


    /**
     * @author dmilith
     *
     * Adds a hook for SIGINT/ SIGTERM signals to gracefully close the app
     *
     * @example
     * addShutdownHook {
     *     closeMySockets
     *     logger.info("Dying!")
     * }
     *
     */
    def addShutdownHook(block: => Unit) =
        Runtime.getRuntime.addShutdownHook(new Thread {
            override def run = block
        })


    /**
     * @author dmilith
     *
     * Returns size (in bytes) of given object in JVM memory
     *
     * @example
     * logger.info ( sizeof ( new Date ( ) ))
     *
     */
    def sizeof(any: Any) = {
        ObjectProfiler.sizeof(any)
    }


    // def pathsToSearchForExecutables = Array(
    //  new File("/opt/local/bin/"), // XXX: hardcoded paths
    //  new File("/bin/"),
    //  new File("/usr/bin/"),
    //  new File("/usr/local/bin/"),
    //  new File("/sw/bin/"),
    //  new File(System.getProperty("user.home") + "/bin"),
    //  new File(System.getProperty("user.home") + "/Bin")
    // )
    //
    // def requirements = Array(
    //  ("git", "gitExecutable"), // XXX: hardcoded executables
    //  ("jarsigner", "jarSignerExecutable"),
    //  ("jar", "jarExecutable")
    // )

    // def autoDetectRequirements = {
    //  for (i <- 0 until requirements.size)
    //    if (!(new File(props(requirements(i)._2) getOrElse mainConfigFile).exists)) {
    //      val al = new ArrayList[File]()
    //      if (System.getProperty("os.name").contains("Linux") ||
    //        System.getProperty("os.name").contains("Mac")) {
    //        for (path <- pathsToSearchForExecutables) {
    //          if (path.exists) {
    //            findFile( path, new P {
    //              override
    //              def accept(t: String): Boolean = {
    //                val fileRegex = ".*" + requirements(i)._1 + "$"
    //                val pattern = Pattern.compile(fileRegex)
    //                val mat = pattern.matcher(t)
    //                if ( mat.find ) return true
    //                return false
    //              }
    //            }, al)
    //          }
    //        }
    //        try {
    //             // props("", value) = Some(requirements(i)._2, al.toArray.first.toString)
    //        } catch {
    //          case x: NoSuchElementException => {
    //            logger.error(requirements(i)._1 + " executable not found")
    //          }
    //        }
    //      } else {
    //        logger.error("Windows hosts wont be supported")
    //        exit(1)
    //      }
    //    }
    // }

}