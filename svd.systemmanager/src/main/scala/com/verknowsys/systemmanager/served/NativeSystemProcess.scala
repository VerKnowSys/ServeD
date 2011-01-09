package com.verknowsys.served.systemmanager

import org.hyperic.sigar._
import com.sun.jna.{Native, Library}


/**
 * Class which describe any system process
 * 
 * @author dmilith
 */
class NativeSystemProcess(val pid: Int) {
    
    
    private val core = new Sigar
    private val stat = core.getProcState(pid)
    private val file = core.getProcExe(pid)
    
    
    val name: String = stat.getName
    val ppid: Long = stat.getPpid
    val thr: Long = stat.getThreads
    val prio: Long = stat.getPriority
    val nice: Long = stat.getNice
    val cwd: String = file.getCwd
    
    // val 
        
    override def toString = "N: %s, PID: %d, PPID: %d ".format(name, pid, ppid)
}