package com.verknowsys.served.systemmanager

import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.monitor._
import com.verknowsys.served.systemmanager.native._

import com.sun.jna.{Native, Library}
import scala.collection.JavaConversions._
import java.io._
import scala.io._
import java.lang.reflect.{Field}
import akka.util.Logging
import org.apache.commons.io.FileUtils


class ProcessException(x: String) extends Exception(x)


/**
  * This class defines mechanism which system commands will be executed through (and hopefully monitored)
  *
  * @author dmilith
  */

class SvdProcess(
    val command: String,
    val user: String = SvdConfig.noUser,
    val workDir: String = SvdConfig.tmp,
    val outputRedirectDestination: String = SvdConfig.nullDevice,
    val useShell: Boolean = false)
        extends Logging {
    
    log.trace("Command(%s) not empty? %s".format(command, commandNotEmpty))
    
    require(commandNotEmpty, "SvdProcess require non-empty command to execute!")
    require(workDirExists, "SvdProcess working dir must exist! Given: %s".format(workDir))
    require(outputWritable, "SvdProcess output file (%s) isn't writable!".format(outputRedirectDestination))
    
    // 2011-01-20 02:42:12 - dmilith - TODO: implement SvdProcess requirements
    // require(userListed)
    
    
    log.debug("Spawning SvdSystemProcess: (%s)".format(command))
    
    Native.setProtected(true)

    lazy val pid = process
    
    log.trace("Process %s spawned.".format(command))

    
        
    def commandNotEmpty =
        (command != "") && (command != null)
        

    def workDirExists =
        new File(workDir).exists
        
    
    def outputWritable = {
        try { 
            FileUtils.touch(outputRedirectDestination)
        } catch {
            case _ =>
        }
        new File(outputRedirectDestination).canWrite
    }
        
        

    // 2011-01-20 01:11:06 - dmilith - TODO: find out is this a most efficient way:
    def alive = 
        try {
	        new SvdSystemProcess(pid)
	        true
        } catch { 
            case x: Any =>
                log.trace("SvdProcess: '%s' has just thrown '%s' in alive()".format(command, x))
                false
        }
        
    
    override def toString = "cmdSvdProc: '%s', pid: %s".format(command, if (pid > 0) pid else "NSY") // 2011-01-20 13:39:07 - dmilith - NOTE: NS - Not Spawned Yet


    private

    /**
      * Spawns new system process
      *
      * @author dmilith
      *
      * @return spawned process pid
      *
      */
    def process = {
        var aPid = -1L
        val cmdFormats = if (useShell) "%s -u %s -s %s > %s 2>&1" else "%s -u %s %s > %s 2>&1"
        val cmd =  cmdFormats.format("sudo", user, command, outputRedirectDestination).split(" ")
        val rt = Runtime.getRuntime
        val env = SvdConfig.env
        val proc = rt.exec(cmd, env)
        log.trace("CMD: %s".format(cmd.mkString(" ")))
        rt.traceMethodCalls(false)

        proc.getClass.getDeclaredFields.foreach{ f =>
            f.setAccessible(true)
            f.getName match {
                case "pid" =>
                    aPid = f.get(proc).asInstanceOf[Int] // 2011-01-24 16:26:50 - dmilith - HACK! cause Long is real class, but ArrayIndexOutOfBoundException: 0 occurs when value is negative
                    log.debug("Pid: %s (of %s)".format(aPid, command))

                case _ =>

            }
            log.trace(f.getName+"="+f.get(proc))
        }
        
        try {
        	if (proc.exitValue > 0)
	            throw new ProcessException("SvdProcess: '%s' exited abnormally with error code: '%s'. Output info: '%s'".format(command, proc.exitValue,
	                if (outputRedirectDestination != SvdConfig.nullDevice) Source.fromFile(outputRedirectDestination).mkString else "NONE"
	            ))
	    } catch { 
	        case x: IllegalThreadStateException =>
	            log.debug("SvdProcess thread exited. No exitValue given.")
            case x: ProcessException =>
                log.error("%s".format(x))
        }
        
        aPid
    }


}
