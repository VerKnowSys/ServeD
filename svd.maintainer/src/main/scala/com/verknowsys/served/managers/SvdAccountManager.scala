package com.verknowsys.served.managers

import com.verknowsys.served.maintainer.SvdAccount
import akka.actor.Actor
import akka.util.Logging

/**
 * SvdAccount SvdManager - owner of all managers
 * 
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount) extends Actor with Logging {
    // start
    
    // val gitSvdManager = new SvdGitManager(this)
    
    log.trace("Starting SvdAccountManager for account: " + account)
    
    
    def receive = {
        case x =>
            log.debug("got " + x)
            
    }
    
    
    // def act {
    //     loop {
    //         receive {
    //             // case msg: Git.Base => sender ! (gitSvdManager !! msg)
    //             
    //             case Init =>
    //                 info("SvdAccountManager ready")
    //                 reply(Ready)
    //                 
    //             case Quit =>
    //                 info("Quitting SvdAccountManager(" + account.userName + ")")
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
    // override def toString = "SvdAccountManager(" + account.userName + ")"
}
