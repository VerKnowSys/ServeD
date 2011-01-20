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
import scala.io._
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
        extends Logged {
    
    // 2011-01-20 02:42:12 - dmilith - TODO: implement SvdProcess requirements
    // require(commandIsntHarmful)
    // require(workDirExists)
    // require(usedShellValid)
    // require(userListed)
    
    debug("Spawning SystemProcess: (%s)".format(command))
    
    Native.setProtected(true)

    val pid = process
    
    trace("Process %s spawned.".format(command))



    // 2011-01-20 01:11:06 - dmilith - XXX: TODO: find out is this a most efficient way:
    def alive = 
        try {
	        new SystemProcess(pid)
	        true
        } catch { 
            case x: Any =>
                trace("SvdProcess: '%s' has just thrown '%s' in alive()".format(command, x))
                false
        }
        

    private

    /**
      * Spawns new system process
      *
      * @author dmilith
      *
      * @return spawned process pid
      *
      */
    def process: Long = {
        var aPid: Long = 0L
        val t = new Thread {
            override def run = {
                // 2011-01-10 23:33:11 - dmilith - TODO: implement params validation.
                val cmdFormats = if (useShell) "%s -u %s -s %s > %s 2>&1" else "%s -u %s %s > %s 2>&1"
                val cmd =  cmdFormats.format(Config.globalSudoExec, user, command, outputRedirectDestination).split(" ")
                val rt = Runtime.getRuntime
                val env = Config.env
                val proc = rt.exec(cmd, env)
                trace("CMD: %s".format(cmd.mkString(" ")))
                rt.traceMethodCalls(false)

                proc.getClass.getDeclaredFields.foreach{ f =>
                    f.setAccessible(true)
                    f.getName match {
                        case "pid" =>
                            aPid = f.get(proc).asInstanceOf[Int]
                            debug("Pid: %s (of %s)".format(aPid, command))

                        case _ =>

                    }
                    trace(f.getName+"="+f.get(proc))
                }
                
                try { 
                    if (proc.exitValue > 0)
                        throw new RuntimeException("'%s' exited abnormally with error code: '%s'. Output info: '%s'".format(command, proc.exitValue, Source.fromFile(outputRedirectDestination).mkString))
                } catch {
                  case e: IllegalThreadStateException =>
                    trace("Process thrown: %s".format(e.getMessage))
                }
            }
        }
        t.start
        t.join
        aPid
    }


}
