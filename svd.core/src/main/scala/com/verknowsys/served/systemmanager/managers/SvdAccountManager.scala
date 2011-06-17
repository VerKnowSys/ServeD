package com.verknowsys.served.systemmanager.managers

import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.SvdExceptionHandler
import com.verknowsys.served.api._
import com.verknowsys.served.utils.Logging
import com.verknowsys.served.db._
import com.verknowsys.served.utils._

import akka.actor.Actor


case object GetAccount


/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount) extends Actor with SvdExceptionHandler {
    log.info("Starting AccountManager for account: " + account)
    
    val dbServer = new DBServer(9000, account.homeDir / "config")
    
    val gitManager = Actor.actorOf(new SvdGitManager(account, dbServer.openClient))
    self startLink gitManager
    
    // val gatherer = Actor.actorOf(new SvdGatherer(account))
    // self startLink gatherer


    def receive = {
        case Init =>
            log.info("SvdAccountManager received Init.")
            
        case GetAccount => 
            self reply account
            
        case msg: git.Base => 
            gitManager forward msg
    }
    
    override def postStop {
        dbServer.close
    }
}
