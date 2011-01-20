package com.verknowsys.served.systemmanager.native

import org.hyperic.sigar._
import scala.collection.JavaConversions._


/**
 * Class which describe any system process
 * 
 * @author dmilith
*/
class SystemResources {
    
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
        "MEM_USED:[%d],\n" +
        "MEM_FREE:[%d],\n" +
        "MEM_TOTAL[%d],\n" +
        "MEM_USAGE_PERC[%f],\n" +
        "SWAP_USED:[%d],\n" +
        "SWAP_FREE:[%d],\n" +
        "SWAP_TOTAL:[%d],\n" +
        "TCP_CONN_OPEN:[%d],\n" +
        "TCP_FAILED_ATT:[%d],\n" +
        "TCP_IN_ERROR:[%d]\n")
            .format(memUsed, memFree, memTotal, memUsagePercentage, swapUsed, swapFree, swapTotal, tcpConnections, tcpFailedAttempts, tcpInError)
    
}