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


case class GetUsers(val list: List[Account])


case class Account(
  val userName: String = "guest",
  val pass: String = "x",
  val uid: String = "1000",
  val gid: String = "1000",
  val information: String = "No information",
  val homeDir: String = "/home/",
  val shell: String = "/bin/bash"
) {
  
  def this(a: List[String]) = this(
    userName = a(0),
    pass = a(1),
    uid = a(2),
    gid = a(3),
    information = a(4),
    homeDir = a(5),
    shell = a(6)
    )
    
}


object SvdAccountManager extends Actor with Utils {


  def act {
		Actor.loop {
			receive {
			  case Init =>
					logger.debug("AccountManager ready for tasks")
				case Quit =>
					logger.info("Quitting AccountManager…")
					exit
				case GetUsers =>
          logger.debug("Sending Users… ")
					SvdMaintainer ! GetUsers(getUsers)
			  case x: AnyRef =>
					logger.warn("Command not recognized. AccountManager will ignore You: " + x.toString)	
		  }
	  }
  }
  

  private
  def parseUsers(users: List[String]): List[Account] = {
    val userList = for (userLine <- users.filterNot{ _.startsWith("#") })
      yield
      userLine.split(":").foldRight(List[String]()) {
        (a, b) => (a :: b) 
      }
    userList.map{ new Account(_) }
  }


  private
  def getUsers: List[Account] = parseUsers(Source.fromFile(Config.systemPasswdFile, "utf-8").getLines.toList)


}
