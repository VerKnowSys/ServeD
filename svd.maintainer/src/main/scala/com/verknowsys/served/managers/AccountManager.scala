package com.verknowsys.served.managers

import com.verknowsys.served.utils.Logged
import com.verknowsys.served.maintainer.Account
import akka.actor.Actor

/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class AccountManager(val account: Account) extends Actor with Logged {
    // start
    
    // val gitManager = new GitManager(this)
    
    trace("Starting AccountManager for account: " + account)
    
    
    def receive = {
        case x => println("got " + x)
    }
    
    
    // def act {
    //     loop {
    //         receive {
    //             // case msg: Git.Base => sender ! (gitManager !! msg)
    //             
    //             case Init =>
    //                 info("AccountManager ready")
    //                 reply(Ready)
    //                 
    //             case Quit =>
    //                 info("Quitting AccountManager(" + account.userName + ")")
    //                 reply(Ready)
    //                 exit
    //             
    //             case _ => messageNotRecognized(_)
    //         }
    //     }
    // }
    // 
    // /**
    //  * @author teamon
    //  */
    // override def toString = "AccountManager(" + account.userName + ")"
}
