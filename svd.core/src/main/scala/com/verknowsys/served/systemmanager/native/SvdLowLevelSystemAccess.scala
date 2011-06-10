package com.verknowsys.served.systemmanager.native


import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.monitor._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.acl._
import com.verknowsys.served.utils.Logging
import SvdPOSIX._

import org.hyperic.sigar._
import com.sun.jna.{Native, Library}


object SvdLowLevelSystemAccess extends Logging {
    
    val core: Option[Sigar] = core match {
        case Some(x: Sigar) =>
            log.debug("Sigar already defined. Passing current value.")
            Some(x)
        case _ =>
            log.debug("No Sigar defined. Defining new one.")
            Some(new Sigar)
    }
        
    val netstat = core.get.getNetStat
    val net = core.get.getNetInfo
    val mem = core.get.getMem
    val swp = core.get.getSwap
    val tcp = core.get.getTcp
    
    val swapUsed = swp.getUsed
    val swapFree = swp.getFree
    val swapTotal = swp.getTotal
    
    val tcpConnections = tcp.getActiveOpens
    val tcpFailedAttempts = tcp.getAttemptFails
    val tcpInError = tcp.getInErrs
    
    val memFree = mem.getActualFree
    val memUsed = mem.getActualUsed
    val memTotal = mem.getTotal
    val memUsagePercentage = mem.getUsedPercent
    
    
    override def toString =
        (
        "MEM_USED:[%d], " +
        "MEM_FREE:[%d], " +
        "MEM_TOTAL[%d], " +
        "MEM_USAGE_PERC[%f], " +
        "SWAP_USED:[%d], " +
        "SWAP_FREE:[%d], " +
        "SWAP_TOTAL:[%d], " +
        "TCP_CONN_OPEN:[%d], " +
        "TCP_FAILED_ATT:[%d], " +
        "TCP_IN_ERROR:[%d] ")
            .format(memUsed, memFree, memTotal, memUsagePercentage, swapUsed, swapFree, swapTotal, tcpConnections, tcpFailedAttempts, tcpInError)

            
    log.debug("%s has been initialized".format(this.getClass))
    
}