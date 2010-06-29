// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package served

import utils.Utils
import actors.Actor
import org.apache.log4j.{Level, Logger}
import signals.{ProcessMessages, MainLoop, Quit, Init}

/**
 * User: dmilith
 * Date: Dec 6, 2009
 * Time: 2:51:47 AM
 */

/**
 * Actor class
 */
class ServeDActor extends Actor {

	def logger: Logger = Logger.getLogger(classOf[ServeDActor])
	var users = List("dmilith", "guest") // XXX: temporary list
	val checkInterval = 2500 // in ms XXX: should be more for production, but small values will make me see average performance of ServeD


	def checkout(users: List[String]) = {
		
	}

	def act {
		Actor.loop {
			receive {
				case MainLoop => {
					logger.info("Into MainLoop")
					while(true) {
						logger.debug("Processing messages")
						this ! ProcessMessages
						Thread sleep checkInterval
					}
				}
				case Init => {
					logger.info("Initializing ServeD")

					logger.info("ServeD ready to perform tasks")
				}
				case Quit => {
					logger.info("Quitting ServeD")
					exit
				}
				case ProcessMessages => {
					logger.info("Doing checkout for users")
					checkout(users)
				}
				case _ => {
					logger.error("Command not recognized. Sorry ServeD will ignore You")
				}
			}
		}
	}

}


/**
 * Main ServeD loader.
 */
object ServeD extends Utils {

	override def logger: Logger = Logger.getLogger(ServeD.getClass)
	initLogger
	setLoggerLevelDebug(Level.DEBUG)
	val appMonitorD = new ServeDActor
	appMonitorD.start

	def main(args: Array[String]) {

		logger.info("User home dir: " + System.getProperty("user.home"))
		logger.debug("Params: " + args + ". Params size: " + args.length)

		addShutdownHook {
			appMonitorD ! "Quit"
			logger.info("ServeD has ended")
		}

		logger.info("ServeD is loading")
		appMonitorD ! Init
		appMonitorD ! MainLoop
	}

}