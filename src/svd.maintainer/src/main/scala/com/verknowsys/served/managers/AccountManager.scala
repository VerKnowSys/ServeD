package com.verknowsys.served.managers

import scala.actors.Actor
import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.maintainer.Account
import com.verknowsys.served.api._

/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class AccountManager(val account: Account) extends Actor with Utils {
    start
    
    val gitManager = new GitManager(this)
    
    def act {
        loop {
            receive {
                case msg: Git.Base => sender ! (gitManager !! msg)
                
                case Init =>
                    logger.info("AccountManager ready for tasks")
                    
                case Quit =>
                    logger.info("Quitting AccountManager...")
                
                case x: AnyRef =>
                    logger.warn("Command not recognized. AccountManager will ignore it: " + x.toString)
            }
        }
    }
}