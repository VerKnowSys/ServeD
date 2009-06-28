// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot



import java.io.OutputStreamWriter
import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}

import prefs.Preferences
import scala.actors._

object ScalaBot extends Actor {
	
	private var arguments: Array[String] = Array()
	private val logger = Logger.getLogger(ScalaBot.getClass)

	Runtime.getRuntime.addShutdownHook( new Thread {
		override def run = {
			logger.info("Bot shutdown requested.")
			XMPPActor ! 'CloseConnection
			XMPPActor ! 'Quit
			ODBServerActor ! 'Quit
			IRCActor ! 'Quit
			ScalaBot ! 'Quit
			logger.info("Done\n")
		}
	})

	def initLogger = {
		val appender = new ConsoleAppender
		appender.setName(ConsoleAppender.SYSTEM_OUT);
		appender.setWriter(new OutputStreamWriter(System.out))
		val level = Level.INFO
		appender.setThreshold(level)
		appender.setLayout(new PatternLayout("{ %-5p %d : %m }%n"));
		Logger.getRootLogger.addAppender(appender)
	}

	def main(args: Array[String]) {
		if (args.size < 1)
            arguments = Array("./") // set current dir if there's no given path to bot dir
        else
            arguments = args

		initLogger
		logger.info("User home dir: " + System.getProperty("user.home"))
		logger.info("Working dir: " + System.getProperty("user.dir"))
		logger.info("Initializing scalaBot..")
		this.start
	}
	
	override def act = {

		ODBServerActor.start
		ODBServerActor ! 'Init
		XMPPActor.start
		XMPPActor ! 'Init
		IRCActor.start
		
		react {
			case 'MainLoop => {
				Actor.loop {
					Thread sleep 500 // 500 ms for each check. That's enough even for very often updated repository
					XMPPActor ! 'ProcessMessages
				}
			}
			case 'Quit => {
				exit
			}
		}
		logger.info("Ready to serve. waiting for orders.")
	}
	
}