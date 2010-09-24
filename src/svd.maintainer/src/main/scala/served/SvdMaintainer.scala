// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer


import com.verknowsys.served.Config
import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.signals.{ProcessMessages, MainLoop, Quit, Init}
import actors.Actor
import org.apache.log4j.{Level, Logger}

/**
 * User: dmilith
 * Date: Dec 6, 2009
 * Time: 2:51:47 AM
 */

/**
 * Actor class
 */
class SvdMaintainer extends Actor with Utils {


	val users = List("dmilith", "guest") // XXX: temporary list
	val checkInterval = 25000 // in ms XXX: should be more for production, but small values will make me see average performance of Maintainer


  def initialize {
    
  }
  
  
	def checkout(users: List[String]) {
		
	}


	def act {
		Actor.loop {
			receive {
				case MainLoop =>
					logger.info("SvdMaintainer is up")
					while(true) {
					  if (props.bool("debug") == true)
						  logger.debug("…")
						this ! ProcessMessages
						Thread sleep checkInterval
					}
				case Init =>
					logger.info("Initializing..")
					initialize
					logger.info("SvdMaintainer ready for tasks")
				case Quit =>
					logger.info("Quitting SvdMaintainer")
					exit
				case ProcessMessages =>
					logger.info("Doing checkout for users: " + users.mkString(", "))
					checkout(users)
				case _ =>
					logger.error("Command not recognized. SvdMaintainer will ignore You")
			}
		}
	}

}


/**
 * Main Maintainer loader.
 */
object SvdMaintainer extends Utils {

  threshold(Level.DEBUG)
  
	val maintainer = new SvdMaintainer
	maintainer.start


	def main(args: Array[String]) {
    
    checkOrCreateVendorDir
    writeDefaultConfig
    
		logger.info("Home dir: " + Config.home)
		logger.debug("Params: " + args.mkString(", ") + ". Params size: " + args.length)
		
		addShutdownHook {
			maintainer ! "Quit"
			logger.info("Maintainer has ended")
		}

		logger.info("Maintainer is loading")
		maintainer ! Init
		maintainer ! MainLoop
	}

}