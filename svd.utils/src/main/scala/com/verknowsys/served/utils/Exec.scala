// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved. 
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils


import java.io._


class Exec extends Utils {

    
    /**
      * Executes process and blocks thread
      * @author dmilith
      *
      * @result (output: String, exitCode: Int)
      *
      */
    def process(command: String, user: String = "nobody", workDir: String = "/tmp/"): (String, Int) = {
        // 2011-01-10 23:33:11 - dmilith - TODO: implement params validation.
        val cmd = "/bin/su -l %s -c %s".format(user, command).split(' ')
        var output = ""
        try {
            val process = Runtime.getRuntime.exec(cmd)
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
