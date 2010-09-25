// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

/**
 * User: dmilith
 * Date: Dec 12, 2009
 * Time: 1:34:27 AM
 */

import com.verknowsys.served._
import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.signals._

import actors.Actor
import java.nio.charset.Charset
import scala.io.Source
import org.apache.log4j.{Level, Logger}


case class GetUsers(val some: String = "")


object SvdAccountManager extends Actor with Utils {


  def act {
		Actor.loop {
			receive {
			  case Init =>
					logger.debug("AccountManager ready for tasks")
				case Quit =>
					logger.info("Quitting AccountManager…")
					exit
				case GetUsers(x) =>
          logger.debug("Sending Users… " + getUsers.getClass)
					SvdMaintainer ! GetUsers(getUsers)
					logger.debug("After GetUsers case…")
			  case x: AnyRef =>
					logger.warn("Command not recognized. AccountManager will ignore You: " + x.toString)	
		  }
	  }
  }
  

  private
  def getUsers = Source.fromFile(Config.systemPasswdFile, "utf-8").getLines.mkString("\n")


}
