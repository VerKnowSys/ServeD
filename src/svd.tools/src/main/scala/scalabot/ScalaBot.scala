// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot


import signals.{ProcessMessages, MainLoop, Init, Quit}
import java.io.OutputStreamWriter
import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
import scala.actors._
import utils.Utils
import prefs.Preferences


object ScalaBot extends Actor with Utils {

	override
	def logger = Logger.getLogger(ScalaBot.getClass)
	initLogger
	addShutdownHook {
		XMPPActor ! Quit
		ODBServerActor ! Quit
//		IRCActor ! Quit
		ScalaBot ! Quit
		logger.info("Done\n")
	}
	var prefs: Preferences = null // global preferences

	def main(args: Array[String]) {
		if (args.length == 0) {
			prefs = new Preferences
		} else {
			prefs = new Preferences(args(0)) // initialize preferences based on given argument (project config file)
		}
		setLoggerLevelDebug(if (prefs.getb("debug")) Level.TRACE else Level.INFO)
		logger.info("User home dir: " + System.getProperty("user.home"))
		logger.debug("Params: " + args + ". Params size: " + args.length)
		this.start
	}
	
	override def act = {
		ODBServerActor.start
		ODBServerActor ! Init
		XMPPActor.start
		XMPPActor ! Init
//		IRCActor.start
		
		react {
			case MainLoop => {
				Actor.loop {
					Thread sleep 500 // 500 ms for each check. That's enough even for very often updated repository
					XMPPActor ! ProcessMessages
				}
			}
			case Quit => {
				exit
			}
		}
		logger.info("Ready to serve. waiting for orders.")
	}
	
}