package com.verknowsys.served.systemmanager.native


import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.monitor._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api.acl._
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

    
    /**
    *   @author dmilith
    *   
    *   Returns System Process count.
    *
    *   Arguments:
    *       sort: Boolean. Default: false
    *
    */
    def processCount(sort: Boolean = false) = processList(sort).size


    /**
     *  @author dmilith
     *
     *  Returns process info of given pid
     */
    def getProcessInfo(apid: Long) = {
        try {
         val an = SvdLowLevelSystemAccess.core.get.getProcState(apid)
             (
                "PNAME:[%s] " +
                "USER:[%s] " +
                "RES:[%s] " +
                "SHR:[%s] " +
                "PID:[%s] " +
                "PPID:[%s] " +
                "THREADS:[%s] " +
                "PRIO:[%s] " +
                "NICE:[%s] " +
                "COMMAND:[%s] " +
                "TIME_START:[%s] " +
                "TIME_KERNEL:[%s] " +
                "TIME_TOTAL:[%s] " +
                "TIME_USER:[%s] " +
                "\n")
                    .format(an.getName, SvdLowLevelSystemAccess.core.get.getProcCredName(apid).getUser, SvdLowLevelSystemAccess.core.get.getProcMem(apid).getResident, SvdLowLevelSystemAccess.core.get.getProcMem(apid).getShare, apid, an.getPpid, an.getThreads, an.getPriority, an.getNice, SvdLowLevelSystemAccess.core.get.getProcArgs(apid).mkString(" "), SvdLowLevelSystemAccess.core.get.getProcCpu(apid).getStartTime, SvdLowLevelSystemAccess.core.get.getProcCpu(apid).getSys, SvdLowLevelSystemAccess.core.get.getProcCpu(apid).getTotal, SvdLowLevelSystemAccess.core.get.getProcCpu(apid).getUser)

        } catch { 
            case _ =>
                "NONE"
        }
    }


    /**
     *  @author dmilith
     *
     *  Returns current process pid
     */
    def getCurrentProcessPid = core.get.getPid

    
    /**
     *  @author dmilith
     *
     *   Returns list of processes in system
     */
    def getProcList = core.get.getProcList
    
    
    /**
     *  @author dmilith
     *
     *   Returns process credentials with given process pid
     */
    def getProcCredName(pid: Long) = core.get.getProcCredName(pid)
    def getProcCred(pid: Long) = core.get.getProcCred(pid)
    

    /**
     *  @author dmilith
     *
     *   Returns system load
     */
    def getSystemLoad = core.get.getLoadAverage
    
    
    /**
     *  @author dmilith
     *
     *   Returns disk usage of given location
     */
    def getDiskUsage(location: String) = core.get.getDiskUsage(location)
    
    
    /**
     *  @author dmilith
     *
     *   Returns current system uptime
     */
    def getSystemUptime = core.get.getUptime.getUptime
    
    
    /**
     *  @author dmilith
     *
     *   Returns state of given process pid
     */
    def getProcState(pid: Long) = core.get.getProcState(pid)


    /**
    *   @author dmilith
    *   
    *   Gets List of pids of whole system processes.
    *   
    *   Arguments: 
    *       sort: Boolean. Default: false.
    *           If true then it will return sorted alphabetically list of processes.
    *
    */
    def processList(sort: Boolean = false) = {
        val preList = SvdLowLevelSystemAccess.core.get.getProcList.toList // 2010-10-24 01:09:51 - dmilith - Java "Array" here.
        val sourceList = if (sort) preList.sortWith(_.toInt < _.toInt) else preList
        log.trace("unsorted    : " + preList)
        log.debug("processList : " + sourceList)
        sourceList
    }
    
    
    log.debug("%s has been initialized".format(this.getClass))
    
}