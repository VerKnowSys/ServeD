// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer


import com.verknowsys.served.Config
import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.signals.{ProcessMessages, MainLoop, Quit, Init}

import scala.collection.JavaConversions._
import actors.Actor
import org.apache.log4j.{Level, Logger}

/**
 * User: dmilith
 * Date: Dec 6, 2009
 * Time: 2:51:47 AM
 */


/**
* @author dmilith
* 
* Main Maintainer loader.
* 
*/

object SvdMaintainer extends Actor with Utils {


	def act {
		Actor.loop {
			receive {
				case MainLoop =>
          // Send messages to actors
          SvdAccountManager ! new GetUsers
          
				case Init =>
					logger.debug("Maintainer ready for tasks")
				case Quit =>
					logger.info("Quitting Maintainer…")
					exit
        case GetUsers(x) => 
          logger.warn("Content:\n" + x)
			  case x: AnyRef =>
					logger.warn("Command not recognized. Maintainer will ignore Your signal: " + x.toString)
			}
		}
	}

  
	def main(args: Array[String]) {
    
    val debug = props.bool("debug") getOrElse true
    if (debug) {
      threshold(Level.DEBUG)
      props("debug") = true
    }

  	SvdMaintainer.start
  	SvdAccountManager.start
  	
  	logger.debug("Mainainer object size: " + sizeof(SvdMaintainer))
		logger.debug("Maintainer home dir: " + Config.home + Config.vendorDir)
		logger.debug("Params: " + args.mkString(", ") + ". Params length: " + args.length)
		
		
		addShutdownHook {
			SvdMaintainer !! Quit
			SvdAccountManager !! Quit
		}

		logger.info("Maintainer is loading…")
		SvdMaintainer !! Init
		logger.info("AccountManager is loading…")
		SvdAccountManager !! Init
		
		logger.info("Entering main loop…")
		while(true) {
		  if (props.bool("debug") getOrElse true) {
		    System.out.print("…")
		  }
		  SvdMaintainer ! MainLoop
  		Thread sleep Config.checkInterval  
		}
		
	}


}