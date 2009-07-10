// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package deployer

import java.awt.Point
import java.io.{OutputStreamWriter, File}
import java.util.regex.Pattern
import java.util.{NoSuchElementException, ArrayList}
import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
import prefs.Preferences
import swing.{MainFrame, Frame, SimpleGUIApplication}
import utils.{Utils}
/**
 * User: dmilith
 * Date: Jul 6, 2009
 * Time: 1:33:54 AM
 */


object DeployerGUI extends SimpleGUIApplication with Utils {

	val logger = Logger.getLogger(DeployerGUI.getClass)
	initLogger
	val prefs = new Preferences
	if (prefs.getb("debug")) {
		setLoggerLevelDebug_?(Level.TRACE)
	}

	//	val PATH = System.getProperty("PATH")
//	val command = CommandExec.cmdExec(Array("env")).trim
//	val searchIn = PATH.split(
//		System.getProperty("path.separator")
//	).toArray.foreach { element => new File(element) }
	val searchIn = Array(
		new File("/opt/local/bin/"), // XXX hardcoded paths
		new File("/bin/"),
		new File("/usr/bin/"),
		new File("/usr/local/bin/")
	)
	val requirements = Array(
		("git", "gitExecutable"),
		("jarsigner", "jarSignerExecutable")
	)

	def autoDetectRequirements = {
		for (i <- 0 until requirements.size)
			if (!(new File(prefs.get(requirements(i)._2)).exists)) {
				val al = new ArrayList[File]()
				if (System.getProperty("os.name").contains("Linux") ||
					System.getProperty("os.name").contains("Mac")) {
					for (path <- searchIn) {
						if (path.exists) {
							findFile( path, new P {
								override
								def accept(t: String): Boolean = {
									val fileRegex = ".*" + requirements(i)._1 + "$"
									val pattern = Pattern.compile(fileRegex)
									val mat = pattern.matcher(t)
									if ( mat.find ) return true
									return false
								}
							}, al)
						}
					}
					try {
						prefs.value.update(requirements(i)._2, al.toArray.first.toString)
					} catch {
						case x: NoSuchElementException => {
							logger.error(requirements(i)._1 + " executable not found")
						}
					}
				} else {
					logger.error("Windows hosts not yet supported")
					exit(1)
				}
			}
	}

	def top = new MainFrame {
		addShutdownHook {
			logger.warn("Done")
		}
		title = "Deployer GUI"
		size = (600,750)
		location = new Point(300, 50)
		prepare
	}

	def prepare = {
		autoDetectRequirements
		logger.warn(prefs.get("gitExecutable"))
		logger.warn(prefs.get("jarSignerExecutable"))
	}



}