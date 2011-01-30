package com.verknowsys.served.managers

import com.verknowsys.served.maintainer.SvdAccount
import com.verknowsys.served.utils.SvdExceptionHandler
import com.verknowsys.served.api.Git
import akka.actor.Actor
import akka.util.Logging

case object GetAccount

/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount) extends Actor with SvdExceptionHandler {
    log.trace("Starting AccountManager for account: " + account)
    
    val gitManager = Actor.actorOf(new SvdGitManager(account))
    self startLink gitManager
    
    def receive = {
        case GetAccount => 
            self reply account
            
        case msg: Git.Base => gitManager forward msg
            
        case msg => log.warn("Message not recoginzed: %s", msg)
    }
    
}
