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
    
    
    private val core = new Sigar
    
    
    // 2011-03-12 15:17:32 - dmilith - TODO: implement folder privileges/file/folder existance checking
    private def userPostfix = account.userName / "svd.gather"
    
    
    private def gatherFileLocation = 
        if (SvdUtils.isBSD)
            "/home" / userPostfix
        else
            "/Users" / userPostfix


    private def gather = SvdUtils.loopThread {
        log.debug("Gathering data for: %s".format(account))
        // 2011-03-12 18:21:30 - dmilith - TODO: add real database write/save here
        
        log.trace("gather() time elapsed: %d".format(
            SvdUtils.bench {
                try {
                    val accountWeight = account.accountWeight.getOrElse(0)
                    log.trace("AccountWeight(%s): %s".format(account.userName, accountWeight))
                    
                    val userPs = core.getProcList.filterNot{ p => core.getProcCredName(p).getUser != account.userName }
                    log.trace("UserPs (%s): %s".format(account.userName, userPs.mkString(". ")))

                    val memUsage = userPs.map{ p => core.getProcMem(p).getResident / 1024 / 1024 }
                    log.trace("MemUsage (%s): %sMiB".format(account.userName, memUsage.mkString("MiB, ")))

                    val memUsageAll = memUsage.sum
                    log.trace("MemUsageAll (%s): %sMiB".format(account.userName, memUsageAll))

                    val cpuUsage = userPs.map{ p => core.getProcCpu(p).getTotal }
                    
                    log.trace("CpuUsage (%s): %s".format(account.userName, cpuUsage.map{
                        p =>
                            val h = (p / 3600)
                            val m = (p / 60) - (h * 60)
                            val s = p - (h * 3600) + (m * 60)
                            "%2d:%2d:%2d".format(h, m, s)
                        }.mkString(",\t")))
                    
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