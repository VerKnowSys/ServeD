// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved. 
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import java.io._


object CommandExec {
	
	def cmdExec(cmdLine: Array[String]): String = {
    	var output = ""
    	try {
			val p = Runtime.getRuntime.exec(cmdLine)
	        val input = new BufferedReader(new InputStreamReader(p.getInputStream))
			var line = ""
	        	while (line != null) {
					output += (line + '\n')
					line = input.readLine
				}
			p.waitFor
	        input.close
	        } catch {
				case ex: Throwable => {
			        ex.printStackTrace
				}
			}
	    output
	}
	
}
