package com.verknowsys.served.systemmanager

import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.monitor._
import com.verknowsys.served.systemmanager.native._

import com.sun.jna.{Native, Library}
import scala.actors._
import scala.actors.Actor._
import scala.collection.JavaConversions._
import java.io._
import java.lang.reflect.{Field}

/**
  * This class defines mechanism which system commands will be executed through (and hopefully monitored)
  *
  * @author dmilith
  */


class SvdProcess(
    val command: String = "",
    val user: String = "nobody",
    val workDir: String = "/tmp/",
    val outputRedirectDestination: String = "/tmp/served_redirXXX",
    val useShell: Boolean = true)
        extends CommonActor with Monitored {
    
    
    var pid = -1 // 2011-01-18 14:03:05 - dmilith - XXX: temporary pid holder
    var hasExited = false // 2011-01-18 14:03:20 - dmilith - XXX: temporary hasExited holder
    var exitCode = -1 // 2011-01-18 14:11:34 - dmilith - XXX: temporary exitCode holder
    var systemProcess: SystemProcess = null
    
    start


    def act {
        Native.setProtected(true)
        loop {
            receive {
                case Run =>
                    debug("Spawning SystemProcess(%s)".format(command))
                    process
                    this ! Quit
                
                case Quit =>
                    trace("Process %s spawned.".format(command))
                    exit
                    
                case x: Any =>
                    error("Request for unsupported signal (%s) of SvdProcess: %s".format(x.toString, systemProcess))
                    
            }
        }
    }

    
    
    /**
      * Executes process and blocks thread
      * @author dmilith
      *
      * @result exitCode: Int
      *
      */
    def process {
        // 2011-01-10 23:33:11 - dmilith - TODO: implement params validation.
        val cmdFormats = if (useShell) "%s -u %s -s %s > %s 2>&1" else "%s -u %s %s > %s 2>&1"
        val cmd =  cmdFormats.format(Config.globalSudoExec, user, command, outputRedirectDestination).split(" ")
        trace("CMD: %s".format(cmd.mkString(" ")))
        val rt = Runtime.getRuntime
        rt.traceMethodCalls(false)
    
        val env = Config.env
        val proc = rt.exec(cmd, env)

        Thread.sleep(100) // 2011-01-18 15:44:11 - dmilith - XXX: wait for shell.. this sucks a bit right?
        
        // 2011-01-18 14:32:02 - dmilith - NOTE: HACK: XXX: matching for required fields twice, cause of unfavorable fields order (from reflection)
        proc.getClass.getDeclaredFields.foreach{ f =>
            f.setAccessible(true)
            f.getName match {
                case "pid" =>
                    pid = f.get(proc).asInstanceOf[Int]
                    systemProcess = new SystemProcess(pid)
                    debug("Pid: %s (of %s)".format(pid, command))
                
                case "hasExited" =>
                    hasExited = f.get(proc).asInstanceOf[Boolean]
                    if (hasExited) systemProcess = null
                    debug("HasExited: %s (of %s)".format(hasExited, command))
                
                case _ =>
            
            }
        }
        // 2011-01-18 14:32:02 - dmilith - NOTE: HACK: XXX: matching for required fields twice, cause of unfavorable fields order (from reflection)
        proc.getClass.getDeclaredFields.foreach{ f =>
            f.setAccessible(true)
            f.getName match {
                case "exitcode" =>
                    exitCode = if (hasExited) f.get(proc).asInstanceOf[Int] else -1 // throw -1 when process is still running
                    debug("ExitCode: %s (of %s)".format(if (exitCode == -1) "still RUNNING process: %s".format(systemProcess) else exitCode, command))
                    if (exitCode > 0)
                        throw new Exception("'%s' exited abnormally.".format(command))
                case _ =>
            
            }
            trace(f.getName+"="+f.get(proc))
        }
        debug("Process: (%s) spawned in background".format(command))
        // error("Process (%s) exception: %s".format(command, ex.getMessage))
    }
            
    
}