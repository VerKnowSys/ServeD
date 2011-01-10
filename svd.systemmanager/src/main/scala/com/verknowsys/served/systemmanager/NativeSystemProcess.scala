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
        ("\n" +
        "PNAME:[%s],\n" +
        "USER:[%s],\n" +
        "RES:[%s],\n" +
        "SHR:[%s],\n" +
        "PID:[%s],\n" +
        "PPID:[%s],\n" +
        "THREADS:[%s],\n" +
        "PRIO:[%s],\n" +
        "NICE:[%s],\n" +
        "COMMAND:[%s],\n" +
        "TIME_START:[%s],\n" +
        "TIME_KERNEL:[%s],\n" +
        "TIME_TOTAL:[%s],\n" +
        "TIME_USER:[%s],\n" +
        "OPEN_FILES:[%s]\n")
            .format(name, user, rss, shr, pid, ppid, thr, prio, nice, params.mkString(" "), timeStart, timeKernel, timeTotal, timeUser, openFiles)
    
}
