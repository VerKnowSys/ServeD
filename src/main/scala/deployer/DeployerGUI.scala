// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package deployer

import java.awt.Dimension
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
	private val prefs = (new Preferences).loadPreferences

	def top = new MainFrame {
		Deployer.initLogger
		Deployer.addShutdownHook {
			logger.warn("Done")
		}
		title = "Deployer GUI"
		size = new Dimension(600,750)
	}



}