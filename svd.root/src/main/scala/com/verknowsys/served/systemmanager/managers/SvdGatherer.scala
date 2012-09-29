package com.verknowsys.served.systemmanager.managers

import com.verknowsys.served.managers.SvdManager

import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.api._
import com.verknowsys.served.db._
import com.verknowsys.served.utils.Logging

import org.apache.commons.io.FileUtils
import akka.actor._


/**
 *  @author dmilith
 *
 *   Gatherer will be spawned for each user account by SvdAccountManager.
 *   The main goal for SvdGatherer is to gather user usage stats and write them to file.
 *
 */
class SvdGatherer(account: SvdAccount) extends SvdManager {
    log.info("Starting Gatherer for account: " + account)


    // private def gather = SvdUtils.loopThread {
    //     log.trace("Time elapsed on gather(): %d".format(
    //         SvdUtils.bench {
    //             try {
    //                 val core = new Sigar
    //                 val userPs = core.getProcList.filter{ p => core.getProcCredName(p).getUser == account.userName }
    //                 log.trace("UserPs (%s): %s".format(account.userName, userPs.mkString(", ")))
    //                 val userPsWithAllData = userPs.map{
    //                     pid => ( // 2011-03-13 02:30:02 - dmilith - NOTE: tuple with whole major user data:
    //                         pid,
    //                         core.getProcState(pid).getName, // NOTE: process name
    //                         core.getProcCpu(pid).getTotal / 1000, // NOTE: time in seconds
    //                         core.getProcMem(pid).getResident / 1024 / 1024 // NOTE: unit is MegaByte.
    //                     )
    //                 }
    //
    //                 // 2011-03-13 15:24:53 - dmilith - NOTE: appending data to user process database
    //                 // val db = new DB
    //                 //                     userPsWithAllData.foreach{
    //                 //                         rec =>
    //                 //                             val value = PSData(rec._1.toInt, rec._2, rec._3.toInt, rec._4.toInt)
    //                 //                             db << value
    //                 //                             log.trace("DB single object size: %d".format(SvdUtils.sizeof(value)))
    //                 //                     }
    //
    //
    //                 // log.trace("DB objects in db: %d".format(db.all[PSData].toList.length))
    //                 // log.trace("DB stats: %s".format(db.current.stats))
    //
    //                 // db.close
    //
    //                 log.debug("userData of (%s):\n%s".format(
    //                     account,
    //                     userPsWithAllData.map{
    //                         elem =>
    //                             "%10d - %30s - %15s - %10d MiB".format(
    //                                 elem._1, elem._2, SvdUtils.secondsToHMS(elem._3.toInt), elem._4) // 2011-03-13 03:28:20 - dmilith - NOTE: in very unusual cases it may lead to truncation of Long value, but I've never ever seen pid bigger than Integer value.
    //                     }.mkString("\n")
    //                 ))
    //
    //             } catch {
    //                 case x: SigarException =>
    //                     log.warn("Gather(), Sigar Exception occured: %s".format(x.getMessage))
    //             }
    //         }
    //     ))
    //
    //     Thread.sleep(SvdConfig.gatherTimeout)
    //
    // }.start

    override def preStart {
        // gather
    }


    def receive = {
        case Init =>
            sender ! Success

        case x =>
            log.warn("SvdGatherer received unhandled signal: %s".format(x))
            sender ! Error("SvdGatherer unknown signal")

    }

}