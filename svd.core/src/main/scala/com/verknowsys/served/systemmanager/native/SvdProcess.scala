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
import scala.collection.JavaConversions._
import scala.io._
import java.io._
import java.lang.reflect.{Field}
import org.apache.commons.io.FileUtils


class ProcessException(x: String) extends Exception(x)


/**
  * This class defines mechanism which system commands will be executed through (and hopefully monitored)
  *
  * @author dmilith
  */

class SvdProcess(
    val command: String,
    val user: String = SvdConfig.noUser,
    val workDir: String = SvdConfig.tmp,
    val waitFor: Boolean = false,
    val shutdownHook: Unit = {})
        extends Logging {

    
    log.debug("Spawning SvdProcess: (%s)".format(command))

    import SvdProcess._

    // 2011-01-26 12:36:06 - dmilith - NOTE: TODO: check low level way of launching processes
    // val pid = {
    //         import CLibrary._
    //         val clib = CLibrary.instance
    //         if (clib.execve("/usr/local/bin/", "sudo -u %s %s".format(user, command).split(" "), Array("")) == 0)
    //             clib.fork.asInstanceOf[Long]
    //         else
    //             -1
    //     }


    /**
      * Spawns new system process
      *
      * @author dmilith
      *
      * @return spawned process pid
      *
      */
    val pid = {
        val cwd = System.getProperty("user.dir")
        System.setProperty("user.dir", workDir)
        log.trace("USER.DIR = %s", System.getProperty("user.dir"))
        var aPid = -1L
        val cmdFormats = "%s -H -u %s %s"
        val cmd =  cmdFormats.format("sudo", user, command).split(" ")
        val rt = Runtime.getRuntime
        rt.addShutdownHook(new Thread {
            override def run = {
                log.info("Shutdown hook on process: %s".format(this))
                shutdownHook
            }
        })
        val env = SvdConfig.env
        val proc = rt.exec(cmd, env)
        log.trace("CMD: %s".format(cmd.mkString(" ")))
        rt.traceMethodCalls(true)
        proc.getClass.getDeclaredFields.foreach{ f =>
            f.setAccessible(true)
            f.getName match {
                case "pid" =>
                    aPid = f.get(proc).asInstanceOf[Int]
                    log.trace("PID: %s (of %s)".format(aPid, command))
                case _ =>
            }
            log.trace(f.getName+"="+f.get(proc))
        }
        if (waitFor) // 2011-03-05 16:59:47 - dmilith - NOTE: synchronized process, waiting until process ends
            proc.waitFor
            
        try {
            if (proc.exitValue > 0)
                log.debug("SvdProcess: '%s' exited abnormally with error code: '%s'.".format(
                    command, proc.exitValue))
        } catch {
            case x: IllegalThreadStateException =>
                log.debug("Process hasn't exited. It should be spawned in background")
        }
        System.setProperty("user.dir", cwd) 
        aPid
    }
    
    
    /**
     *  @author dmilith
     *
     *   Accessors for process detail info
     */
    def stat: ProcState = try {
    	core.getProcState(pid)
    } catch { 
        case x: SigarException =>
            log.debug("Sigar has just thrown: %s".format(x))
            new ProcState
    }
    
    
    def cpu = try {
    	core.getProcCpu(pid)
    } catch { 
        case x: SigarException =>
            log.debug("Sigar has just thrown: %s".format(x))
            new ProcCpu
    }
    
    
    def mem = try {
    	core.getProcMem(pid)
    } catch { 
        case x: SigarException =>
            log.debug("Sigar has just thrown: %s".format(x))
            new ProcMem
    }
    

    def params: Array[String] = try {
    	core.getProcArgs(pid)
    } catch { 
        case _ =>
            Array("")
    }

    
    def name = stat.getName
    def runUser = core.getProcCredName(pid).getUser
    def ppid = stat.getPpid
    def thr = stat.getThreads
    def prio = stat.getPriority
    def nice = stat.getNice
    def timeStart = cpu.getStartTime
    def timeKernel = cpu.getSys
    def timeTotal = cpu.getTotal
    def timeUser = cpu.getUser
    def rss = mem.getResident
    def shr = mem.getShare


    /**
     *  @author dmilith
     *
     *   Requirements for spawning process
     */
    require(commandNotEmpty, "SvdProcess require non-empty command to execute!")
    require(workDirExists, "SvdProcess working dir must exist! Given: %s".format(workDir))
    require(passACLs, "SvdProcess didn't pass ACL requirements! Failed process: %s".format(command))
    require(pid > 0, "SvdProcess PID always should be > 0!")
    
    // 2011-01-20 02:42:12 - dmilith - TODO: implement SvdProcess requirements
    // require(userListed)


    /**
     *  @author dmilith
     *
     *   All ACLs must pass for given process
     */
    def passACLs = SvdAccount.aclFor(name)
    
    
    /**
     *  @author dmilith
     *
     *   Checks for empty command
     */
    def commandNotEmpty =
        (command != "") && (command != null)


    /**
     *  @author dmilith
     *
     *   Checks for dir existance
     */
    def workDirExists =
        new File(workDir).exists
            

    /**
     *  @author dmilith
     *
     *   Returns true if current process exists
     */    
    def alive = {
        while (pid == -1L)
            Thread.sleep(10)
        if (SvdProcess.getProcessInfo(pid) == "NONE") 
            false
        else
            true
    }
    

    /**
      * Kills system process
      *
      * @author dmilith
      *
      * @return true if succeeded, false if failed
      *
      */
    def kill(signal: SvdPOSIX.Value = SIGINT) = {
        while (pid == -1L)
            Thread.sleep(10)
        if (SvdProcess.getProcessInfo(pid) != "NONE") 
            SvdProcess.kill(pid, signal)
        else
            true // already killed so we might return true
    }
        


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
           "\n")
               .format(name, user, rss, shr, pid, ppid, thr, prio, nice, command, timeStart, timeKernel, timeTotal, timeUser)


    log.trace("Process %s spawned.".format(command))
    
}


/**
 *  @author dmilith
 *
 *  Static access to kill process from outside of process
 */
object SvdProcess extends Logging {
    
    
    val core = new Sigar
    
    
    /**
      * Kills system process with given pid and signal
      *
      * @author dmilith
      *
      * @return true if succeeded, false if failed
      *
      */
    def kill(pid: Long, signal: SvdPOSIX.Value = SIGINT) = {
        import CLibrary._
        val clib = CLibrary.instance
        if (clib.kill(pid, signal.id) == 0)
            true
        else
            false
    }
    
    
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
        val preList = core.getProcList.toList // 2010-10-24 01:09:51 - dmilith - Java "Array" here.
        val sourceList = if (sort) preList.sortWith(_.toInt < _.toInt) else preList
        log.trace("unsorted    : " + preList)
        log.debug("processList : " + sourceList)
        sourceList
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
    

    /**
     *  @author dmilith
     *
     *  Returns process info of given pid
     */
    def getProcessInfo(apid: Long) = {
        try {
         val an = core.getProcState(apid)
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
                    .format(an.getName, core.getProcCredName(apid).getUser, core.getProcMem(apid).getResident, core.getProcMem(apid).getShare, apid, an.getPpid, an.getThreads, an.getPriority, an.getNice, core.getProcArgs(apid).mkString(" "), core.getProcCpu(apid).getStartTime, core.getProcCpu(apid).getSys, core.getProcCpu(apid).getTotal, core.getProcCpu(apid).getUser)
         
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
    def getCurrentProcessPid =
        core.getPid


}