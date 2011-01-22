package com.verknowsys.served.managers

import com.verknowsys.served.maintainer.SvdAccount
import akka.actor.Actor
import akka.util.Logging

/**
 * SvdAccount SvdManager - owner of all managers
 * 
 * @author teamon
 */
class SvdAccountSvdManager(val account: SvdAccount) extends Actor with Logging {
    // start
    
    // val gitSvdManager = new SvdGitSvdManager(this)
    
    log.trace("Starting SvdAccountSvdManager for account: " + account)
    
    
    def receive = {
        case x => println("got " + x)
    }
    
    
    // def act {
    //     loop {
    //         receive {
    //             // case msg: Git.Base => sender ! (gitSvdManager !! msg)
    //             
    //             case Init =>
    //                 info("SvdAccountSvdManager ready")
    //                 reply(Ready)
    //                 
    //             case Quit =>
    //                 info("Quitting SvdAccountSvdManager(" + account.userName + ")")
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
    // override def toString = "SvdAccountSvdManager(" + account.userName + ")"
}
