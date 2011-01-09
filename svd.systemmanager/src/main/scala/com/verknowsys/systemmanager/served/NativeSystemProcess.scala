package com.verknowsys.served.systemmanager

import org.hyperic.sigar._
import scala.collection.JavaConversions._

/**
 * Class which describe any system process
 * 
 * @author dmilith
 */
class NativeSystemProcess(val pid: Long) {
    
    private val core = new Sigar
    private val stat = core.getProcState(pid)
    private val cpu = core.getProcCpu(pid)
    private val mem = core.getProcMem(pid)
    
    
    val name: String = stat.getName
    val user: String = core.getProcCredName(pid).getUser
    val ppid: Long = stat.getPpid
    val thr: Long = stat.getThreads
    val prio: Long = stat.getPriority
    val nice: Long = stat.getNice
    val params: Array[String] = core.getProcArgs(pid)
    
    val timeStart: Long = cpu.getStartTime
    val timeKernel: Long = cpu.getSys
    val timeTotal: Long = cpu.getTotal
    val timeUser: Long = cpu.getUser
    
    // val env = core.getProcEnv(pid).toMap
    
    val rss: Long = mem.getResident
    val shr: Long = mem.getShare
    
    val openFiles: Long = -1
    
    override def toString = "NAME:[%s], USER:[%s] RES:[%d], SHR:[%d], PID:[%d], PPID:[%d], THREADS:[%d], PRIO:[%d], NICE:[%d], PARAMS:[%s], TIME_START:[%d], TIME_KERNEL:[%d], TIME_TOTAL:[%d], TIME_USER:[%d], OPEN_FILES:[%d]\n\n".format(name, user, rss, shr, pid, ppid, thr, prio, nice, params.mkString(" "), timeStart, timeKernel, timeTotal, timeUser, openFiles)
    
}
