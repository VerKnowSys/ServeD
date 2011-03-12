package com.verknowsys.served.systemmanager.managers

import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.api._

import org.hyperic.sigar._
import akka.actor.Actor
import akka.actor.Actor.actorOf
import akka.actor.Actor.registry
import akka.util.Logging


/**
 *  @author dmilith
 *
 *   Gatherer will be spawned for each user account by SvdAccountManager.
 *   The main goal for SvdGatherer is to gather user usage stats and write them to file.
 *
 */
class SvdGatherer(account: SvdAccount) extends SvdManager(account) {
    
    
    // 2011-03-12 15:17:32 - dmilith - TODO: implement folder privileges/file/folder existance checking
    
    def userPostfix = account.userName / "svd.gather"
    
    
    def gatherFileLocation = 
        if (SvdUtils.isBSD)
            "/home" / userPostfix
        else
            "/Users" / userPostfix


    log.info("Starting SvdGatherer for account: %s. Account gath file: %s".format(account, gatherFileLocation))
    
    def receive = {
        case Init =>
            log.trace("gatherFileLocation: %s".format(gatherFileLocation))
            log.debug("Initializing SvdGatherer for user %s".format(account.userName))
            
        case x =>
            log.trace("gatherFileLocation: %s".format(gatherFileLocation))
            log.debug("Received unknown signal in SvdGatherer: %s".format(x))
    }
 
    
}