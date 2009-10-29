//// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
//// This Software is a close code project. You may not redistribute this code without permission of author.
//
//package deployer
//
//import java.awt.Point
//import java.io.{OutputStreamWriter, File}
//import java.util.regex.Pattern
//import java.util.{NoSuchElementException, ArrayList}
//import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
//import prefs.Preferences
//import swing.{MainFrame, Frame, SimpleGUIApplication}
//import utils.{Utils}
///**
// * User: dmilith
// * Date: Jul 6, 2009
// * Time: 1:33:54 AM
// */
//
//
//object DeployerGUI extends SimpleGUIApplication with Utils {
//
//	override
//	def logger = Logger.getLogger(DeployerGUI.getClass)
//	initLogger
//	lazy val prefs = Deployer.prefs
//	if (prefs.getb("debug")) {
//		setLoggerLevelDebug(Level.TRACE)
//	}
//
//	def top = new MainFrame {
//		addShutdownHook {
//			logger.warn("Done")
//		}
//		title = "Deployer GUI"
//		size = (600,750)
//		location = new Point(300, 50)
//		prepare
//	}
//
//	def prepare = {
//		autoDetectRequirements(prefs)
//		logger.warn(prefs.get("gitExecutable"))
//		logger.warn(prefs.get("jarSignerExecutable"))
//	}
//
//
//
//}