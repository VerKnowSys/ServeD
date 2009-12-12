// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package jar


import java.io.{InputStreamReader, BufferedReader}

/**
 * User: dmilith
 * Date: Oct 3, 2009
 * Time: 7:52:04 PM
 */

object JarAccess {

	def readLineFromJARFile(filename: String) = {
		lazy val is = getClass.getResourceAsStream(filename)
		lazy val isr = new InputStreamReader(is)
		lazy val br = new BufferedReader(isr)
		lazy val sb = new StringBuffer
		var line = ""
		try {
			sb.append(br.readLine) // only one - first line is interesting
		} catch {
			case x: Exception =>
		}
		br.close
		isr.close
		is.close
		sb.toString
	}


}