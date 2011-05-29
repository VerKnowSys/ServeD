package com.verknowsys.served.systemmanager.managers


import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.SvdExceptionHandler
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.api.Git
import com.verknowsys.served.utils.Logging

import akka.actor.Actor


case object GetAccount


/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount) extends Actor with SvdExceptionHandler {
    log.info("Starting AccountManager for account: " + account)
    
    val gitManager = Actor.actorOf(new SvdGitManager(account))
    self startLink gitManager
    
    val gatherer = Actor.actorOf(new SvdGatherer(account))
    self startLink gatherer
    

    def receive = {
        case Init =>
            log.info("SvdAccountManager received Init.")
            
        case GetAccount => 
            self reply account
            
        case msg: Git.Base => gitManager forward msg
            
        case msg => log.warn("Message not recoginzed: %s", msg)
    }

    
}
