package com.verknowsys.served.systemmanager

import org.hyperic.sigar._
import com.sun.jna.{Native, Library}
import scala.collection.JavaConversions._

/**
 * Class which describe any system process
 * 
 * @author dmilith
 */
class NativeSystemProcess(val pid: Long) {
    
    
    private val core = new Sigar
    private val stat = core.getProcState(pid)
    private val file = core.getProcExe(pid)
    private val cpu = core.getProcCpu(pid)
    private val mem = core.getProcMem(pid)
    
    
    val name: String = stat.getName
    val ppid: Long = stat.getPpid
    val thr: Long = stat.getThreads
    val prio: Long = stat.getPriority
    val nice: Long = stat.getNice
    
    val params: Array[String] = core.getProcArgs(pid)
    val cwd: String = file.getCwd
    
    val timeStart: Long = cpu.getStartTime
    val timeKernel: Long = cpu.getSys
    val timeTotal: Long = cpu.getTotal
    val timeUser: Long = cpu.getUser
    
    val env = core.getProcEnv(pid).toMap
    val residentMem: Long = mem.getResident
    val sharedMem: Long = mem.getShare
    
    
    override def toString = "N: %s, RES: %d, SHR: %d PID: %d, PPID: %d, THR: %d, PRIO: %d, NI: %d, PARAMS: [%s], CWD: [%s], TIME_START: %d, TIME_KERNEL: %d, TIME_TOTAL: %d, TIME_USER: %d, ENV: [%s] ".format(name, residentMem, sharedMem, pid, ppid, thr, prio, nice, params, cwd, timeStart, timeKernel, timeTotal, timeUser, env.mkString("#\n"))
    
}