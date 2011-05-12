package com.verknowsys.served.systemmanager.managers

import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.api._
import com.verknowsys.served.utils.Logging

import org.hyperic.sigar._
import akka.actor.Actor
import akka.actor.Actor.actorOf
import akka.actor.Actor.registry


/**
 *  @author dmilith
 *
 *   Gatherer will be spawned for each user account by SvdAccountManager.
 *   The main goal for SvdGatherer is to gather user usage stats and write them to file.
 *
 */
class SvdGatherer(account: SvdAccount) extends SvdManager(account) {
    
    
    val gatherFilename = "svd.gather"
    
    private val core = new Sigar


    private def gatherFileLocation = SvdUtils.checkOrCreateDir(SvdConfig.homePath + SvdConfig.vendorDir) / gatherFilename


    private def gather = SvdUtils.loopThread {

        // 2011-03-12 18:21:30 - dmilith - TODO: add real database write / save here
        
        log.trace("Time elapsed on gather(): %d".format(
            SvdUtils.bench {
                try {
                    val userPs = core.getProcList.filter{ p => core.getProcCredName(p).getUser == account.userName }
                    log.trace("UserPs (%s): %s".format(account.userName, userPs.mkString(", ")))
                    val userPsWithAllData = userPs.map{
                        pid => ( // 2011-03-13 02:30:02 - dmilith - NOTE: tuple with whole major user data:
                            pid,
                            core.getProcState(pid).getName, // NOTE: process name
                            core.getProcCpu(pid).getTotal / 1000, // NOTE: time in seconds 
                            core.getProcMem(pid).getResident / 1024 / 1024 // NOTE: unit is MegaByte.
                        )
                    }
                    log.debug("userData of (%s):\n%s".format(
                        account,
                        userPsWithAllData.map{
                            elem =>
                                "%10d - %30s - %15s - %10d MiB".format(
                                    elem._1, elem._2, SvdUtils.secondsToHMS(elem._3.toInt), elem._4) // 2011-03-13 03:28:20 - dmilith - NOTE: in very unusual cases it may lead to truncation of Long value, but I've never ever seen pid bigger than Integer value.
                        }.mkString("\n")
                    ))
                } catch {
                    case x: SigarException =>
                        log.warn("Gather(), Sigar Exception occured: %s".format(x.getMessage))
                }
            }
        ))
        Thread.sleep(SvdConfig.gatherTimeout)
        
    }.start
    
    
    log.info("Running SvdGatherer for account: %s. (Account gath file: %s)".format(account, gatherFileLocation))
    gather
    
    
    def receive = {
        case x =>
            log.warn("SvdGatherer received unhandled signal: %s".format(x))
            self reply Nil
            
    }
 
    
}