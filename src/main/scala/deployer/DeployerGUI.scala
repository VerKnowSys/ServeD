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

	var logger = Logger.getLogger(DeployerGUI.getClass)
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
		autoDetectRequirements(prefs)
		logger.warn(prefs.get("gitExecutable"))
		logger.warn(prefs.get("jarSignerExecutable"))
	}



}