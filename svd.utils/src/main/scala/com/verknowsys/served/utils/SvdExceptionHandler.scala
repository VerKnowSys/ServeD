package com.verknowsys.served.utils

import akka.actor.Actor
import akka.util.Logging
import org.apache.commons.io.FileUtils
import java.io._
import java.util.Collection
import java.util.Iterator
import com.verknowsys.served.utils._


/**
 *  @author dmilith
 *
 *   This trait should be used by all ServeD actors
 */
trait SvdExceptionHandler extends Actor with Logging { 
    
    // 2011-01-30 01:36:28 - dmilith - NOTE: txmt protocol example: txmt://open/?url=file://~/.bash_profile&line=11&column=2
    
    /**
     *  @author dmilith
     *
     *   
     */
    override def preRestart(reason: Throwable) = {
        super.preRestart(reason)
        log.trace("preRestart executed in %s".format(this.getClass))
        log.error(
            """
    Restarting Actor: (%s) cause of %s.
    Throwable details: (%s).
            """.format(
                self.getClass.getName, reason.getMessage,
                reason.getStackTrace.map {
                    traceElement =>
                        """
        url             - %s
        class name      - %s
        method name     - %s
        file name       - %s:%s
        native method   - %s
                        """.format(
                                if (traceElement.getFileName.contains("Svd")) // 2011-01-30 03:34:13 - dmilith - NOTE: all project files will include Svd prefix
                                    "txmt://open/?url=file://%s&line=%s".format(
                                        SvdUtils.findFileInDir(traceElement.getFileName),
                                        traceElement.getLineNumber
                                    )
                                else "No source",
                                traceElement.getClassName,
                                traceElement.getMethodName,
                                traceElement.getFileName, traceElement.getLineNumber,
                                traceElement.isNativeMethod
                            )    
                }.mkString
            )
        )
        
    }
    
    
    override def postStop = {
        log.trace("postStop executed in %s".format(this.getClass))
    }
    
    
}