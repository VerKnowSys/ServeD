// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package deployer

import java.awt.Dimension
import java.io.{OutputStreamWriter, File}
import java.util.regex.Pattern
import java.util.{NoSuchElementException, ArrayList}
import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}

import prefs.Preferences
import swing.{MainFrame, Frame, SimpleGUIApplication}

/**
 * User: dmilith
 * Date: Jul 6, 2009
 * Time: 1:33:54 AM
 */


object DeployerGUI extends SimpleGUIApplication {

	private val logger = Logger.getLogger(DeployerGUI.getClass)
	private val prefs = new Preferences
	private val searchIn = Array(
				new File("/opt/local/bin/"), // XXX hardcoded paths
				new File("/bin/"),
				new File("/usr/bin/"),
				new File("/usr/local/bin/")
	)
	private val requirements = Array(
			("git", "gitExecutable"),
			("jarsigner", "jarSignerExecutable")
	)

	def autoDetectRequirements = {
		logger.info("Detecting requirements")
		for (i <- 0 until requirements.size)
			if (!(new File(prefs.get(requirements(i)._2)).exists)) {
				val al = new ArrayList[File]()
				if (System.getProperty("os.name").contains("Linux") ||
					System.getProperty("os.name").contains("Mac")) {
					for (path <- searchIn) {
						if (path.exists) {
							Deployer.findFile( path, new P {
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
		Deployer.initLogger
		Deployer.addShutdownHook {
			logger.warn("Done")
		}
		title = "Deployer GUI"
		size = new Dimension(600,750)
		prepare
	}

	def prepare = {
		autoDetectRequirements
		logger.warn(prefs.get("gitExecutable"))
		logger.warn(prefs.get("jarSignerExecutable"))
	}



}