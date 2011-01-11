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


class SvdSystemProcess(val commandInput: String = "") extends Actor with Monitored with Utils {
    
    private var process: Process = null
    private var output = ""
    

    start


    def act {
        Native.setProtected(true)
        loop {
            receive {
                case Run =>
                    logger.debug("new SystemProcess(%s)".format(commandInput))
                    val results = process(commandInput) // may be blocking call
                    logger.debug("new SystemProcess(%s) results: '%s' '%d'".format(commandInput, results._1, results._2))
                    reply(results)
                    
                case x: Any =>
                    logger.info("Request for unsupported signal of SystemProcess: %s".format(x.toString))
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
    def process(command: String, user: String = "nobody", workDir: String = "/tmp/"): (String, Int) = {
        // 2011-01-10 23:33:11 - dmilith - TODO: implement params validation.
        val cmd = "%s -l %s -c '%s'".format("/usr/bin/su", user, command).split(' ') // 2011-01-11 01:36:29 - dmilith - XXX: hardcode
        logger.trace("CMD: %s".format(cmd.mkString(" ")))
        try {
            process = Runtime.getRuntime.exec(cmd)
            val input = new BufferedReader(new InputStreamReader(process.getInputStream))
            var line = ""
            while (line != null) {
                output += line + "\n"
                line = input.readLine
            }
            process.waitFor
            input.close
            (output, process.exitValue)

        } catch {
            case ex: Throwable => {
                logger.error("Process exception: %s".format(ex.getMessage))
                ("", -1)
            }
        }
    }
            
    
}