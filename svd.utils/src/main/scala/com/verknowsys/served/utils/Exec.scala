// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved. 
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils


import java.io._


object Exec {
    
    
    def blockCommand(cmdLine: String) = {
        var output = ""
        var code = 0
        try {
            val p = Runtime.getRuntime.exec(cmdLine.split(' '))
            val input = new BufferedReader(new InputStreamReader(p.getInputStream))
            var line = ""
            while (line != null) {
                output += (line + '\n')
                line = input.readLine
            }
            p.waitFor
            code = p.exitValue
            input.close
        } catch {
            case ex: Throwable => {
                ex.printStackTrace
            }
        }
    }
    
    
    def noBlockCommand(cmdLine: String): (String, Long) = {
        var output = ""
        var code = 0
        try {
            val p = Runtime.getRuntime.exec(cmdLine.split(' '))
            val input = new BufferedReader(new InputStreamReader(p.getInputStream))
            var line = ""
            while (line != null) {
                output += (line + '\n')
                line = input.readLine
            }
            p.waitFor
            code = p.exitValue
            input.close
        } catch {
            case ex: Throwable => {
                ex.printStackTrace
            }
        }
        (output, code)
    }

}
