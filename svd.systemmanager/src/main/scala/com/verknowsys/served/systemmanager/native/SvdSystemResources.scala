package com.verknowsys.served.systemmanager.native

import org.hyperic.sigar._
import scala.collection.JavaConversions._


/**
 * Class which describe any system process
 * 
 * @author dmilith
*/
class SvdSystemResources {
    
    private val core = new Sigar
    private val mem = core.getMem
    private val swp = core.getSwap
    private val tcp = core.getTcp

   
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
    
}