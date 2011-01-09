package com.verknowsys.served.systemmanager

import org.hyperic.sigar._
import scala.collection.JavaConversions._

/**
 * Class which describe any system process
 * 
 * @author dmilith
*/
class NativeSystemResources {
    
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
    
    override def toString = "# MEM_USED:[%d],\nMEM_FREE:[%d],\nMEM_TOTAL[%d],\nMEM_USAGE_PERC[%f],\nSWAP_USED:[%d],\nSWAP_FREE:[%d],\nSWAP_TOTAL:[%d],\nTCP_CONN_OPEN:[%d],\nTCP_FAILED_ATT:[%d],\nTCP_IN_ERROR:[%d]\n".format(memUsed, memFree, memTotal, memUsagePercentage, swapUsed, swapFree, swapTotal, tcpConnections, tcpFailedAttempts, tcpInError)
    
}