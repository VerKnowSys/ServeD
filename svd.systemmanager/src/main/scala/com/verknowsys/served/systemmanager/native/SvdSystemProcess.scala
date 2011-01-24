package com.verknowsys.served.systemmanager.native

import org.hyperic.sigar._
import scala.collection.JavaConversions._
import akka.util.Logging


/**
 *  @author dmilith
 *
 *  This class gives access to additional native system information unavailable by default from JVM
 * 
 *  Arguments:
 *      pid: Long
 *
 */
class SvdSystemProcess(val pid: Long) extends Logging {
    
    private val core = new Sigar
    private val stat = core.getProcState(pid)
    private val cpu = core.getProcCpu(pid)
    private val mem = core.getProcMem(pid)

    require(pid > 0, "SSP: PID always should be > 0!")
    require(core != null, "SSP: Core cannot be null!")
    require(stat != null, "SSP: Stat cannot be null!")
    require(cpu != null, "SSP: Cpu cannot be null!")
    require(mem != null, "SSP: Mem cannot be null!")
    
    val name = stat.getName
    val user = core.getProcCredName(pid).getUser
    val ppid = stat.getPpid
    val thr = stat.getThreads
    val prio = stat.getPriority
    val nice = stat.getNice
    val params: Array[String] = core.getProcArgs(pid)
    
    val timeStart = cpu.getStartTime
    val timeKernel = cpu.getSys
    val timeTotal = cpu.getTotal
    val timeUser = cpu.getUser
    
    // val env = core.getProcEnv(pid).toMap
    
    val rss = mem.getResident
    val shr = mem.getShare
    
    val openFiles = -1L
    

    override def toString =
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
        "OPEN_FILES:[%s]\n")
            .format(name, user, rss, shr, pid, ppid, thr, prio, nice, params.mkString(" "), timeStart, timeKernel, timeTotal, timeUser, openFiles)


        /**
        *   @author dmilith
        *   
        *   Converts processes as String to List of SvdSystemProcess'es.
        *   
        *   Arguments: 
        *       sort: Boolean. Default: false.
        *           If true then it will return sorted alphabetically list of processes.
        *
        */
        def processList(sort: Boolean = false) = {
            val preList = core.getProcList.toList // 2010-10-24 01:09:51 - dmilith - NOTE: toList, cause JNA returns Java's "Array" here.
            val sourceList = if (sort) preList.sortWith(_.toInt < _.toInt) else preList
            log.debug("UnSORTED   : " + preList)
            log.debug("SORTED     : " + preList.sortWith(_.toInt < _.toInt))
            log.trace("sourceList : " + sourceList)
            
            sourceList.flatMap(
                x =>
                    try {
                	    new SvdSystemProcess(x) :: Nil // 2011-01-23 17:56:26 - dmilith - NOTE: it takes process list of pids + currently spawned test process pid on which "No such process" exception may be thrown. It's 100% normal behaviour
                    } catch {
                        case _ =>
                            Nil
                    }
            )
        }


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

    
}
