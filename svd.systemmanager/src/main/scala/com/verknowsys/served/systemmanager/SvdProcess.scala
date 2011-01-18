package com.verknowsys.served.systemmanager


import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.monitor._

import com.sun.jna.{Native, Library}
import scala.actors._
import scala.actors.Actor._
import scala.collection.JavaConversions._
import java.io._

/**
  * This class defines mechanism which system commands will be executed through (and hopefully monitored)
  *
  * @author dmilith
  */


class SvdProcess(
    val command: String = "",
    val user: String = "nobody",
    val workDir: String = "/tmp/",
    val outputRedirectDestination: String = "/tmp/served_redirXXX")
        extends CommonActor with Monitored {
    
    
    // private var proc: Process = null
    // private var pid = -1

    start


    def act {
        Native.setProtected(true)
        loop {
            receive {
                case Run =>
                    trace("SystemProcess(%s)".format(command))
                    val results = process // may be blocking call
                    trace("SystemProcess(%s) exit code: %d".format(command, results))
                    this ! Quit
                    reply(results)
                
                case Quit =>
                    trace("Done process: %s".format(command))
                    exit
                    
                case Ready =>
                    
                case x: Any =>
                    error("Request for unsupported signal of SystemProcess: %s".format(x.toString))
                    reply(Ready)
                    
            }
        }
    }

    
    
    /**
      * Executes process and blocks thread
      * @author dmilith
      *
      * @result (output: String, exitCode: Int)
      *
      */
    def process: Int = {
        // 2011-01-10 23:33:11 - dmilith - TODO: implement params validation.
        // 2011-01-18 01:55:17 - dmilith - TODO: implement workDir usage.
        val cmd = "%s -l %s -c %s > %s 2>&1".format("/usr/bin/su", user, command, outputRedirectDestination).split(" ")
        trace("CMD: %s".format(cmd.mkString(" ")))
        try {
            val proc = Runtime.getRuntime.exec(cmd)
            trace("Process: (%s) spawned.".format(command))
            proc.waitFor
            val exitValue = proc.exitValue
            trace("Process: (%s) return value: %d".format(command, exitValue))
            exitValue
            
        } catch {
            case ex: Throwable =>
                error("Process exception: %s".format(ex.getMessage))
                -1
        }
    }
            
    
}