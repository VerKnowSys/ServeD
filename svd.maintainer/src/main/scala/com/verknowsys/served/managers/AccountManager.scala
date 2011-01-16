package com.verknowsys.served.managers

import scala.actors.Actor
import com.verknowsys.served.utils.CommonActor
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.maintainer.Account
import com.verknowsys.served.api._

/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class AccountManager(val account: Account) extends CommonActor {
    start
    
    // val gitManager = new GitManager(this)
    
    def act {
        loop {
            receive {
                // case msg: Git.Base => sender ! (gitManager !! msg)
                
                case Init =>
                    info("AccountManager ready")
                    reply(Ready)
                    
                case Quit =>
                    info("Quitting AccountManager(" + account.userName + ")")
                    reply(Ready)
                    exit
                
                case _ => messageNotRecognized(_)
            }
        }
    }
    
    /**
     * @author teamon
     */
    override def toString = "!AccountManager(" + account.userName + ")!"
}
