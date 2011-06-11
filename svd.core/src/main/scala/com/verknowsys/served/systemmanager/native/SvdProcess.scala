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


/**
  * This class defines mechanism which system commands will be executed through (and hopefully monitored)
  *
  * @author dmilith
  */

class SvdProcess(
    val command: String,
    val uid: Int,
    val workDir: String = SvdConfig.systemTmpDir,
    val outputDir: String = SvdConfig.systemTmpDir,
    val shutdownHook: Unit = {})
        extends Logging {


    var taken = ""
    
    log.debug("Spawning SvdProcess: (%s)".format(command))
    val spawnerThread = new Thread {
        override def run = {
            // PENDING: TODO: XXX: perform chdir(workDir) before spawning process (add additional param to native spawn())
            log.trace("Spawning SvdProcess Thread")
            val wrapper = SvdWrapLibrary.instance
            taken = wrapper.spawn(uid, command, outputDir / "%s.log".format(uid))
        }
    }
    spawnerThread.start
    spawnerThread.join
    
    log.trace("SvdProcess spawned (%s)".format(taken))
    
    private val tt = taken.split(";")
    tt.head.toInt match {
        case 250 | 251 | 252 | 253 | 254 =>
            log.error("Exception executing process: %s".format(this))
            throw new Exception("Process with command: '%s' failed: %s".format(command, this))
            
        case x: Any =>
            
    }

    log.trace("Taken output of spawn(): %s".format(tt))

    val ppid: Int = tt.headOption match {
        case None | Some("") =>
            -1
            
        case Some(x) =>
            log.trace("PPID: %s".format(ppid))
            x.toInt
        
    }
    val pid: Int = tt.tail.headOption match {
        case None | Some("") =>
            -1
            
        case Some(x) =>
            log.trace("PID: %s".format(ppid))
            x.toInt
        
    }

    
    /**
     *  @author dmilith
     *
     *   Requirements for spawning process
     */
    require(uidGreaterOrEqualZero, "SvdProcess given uid value must be >= 0")
    require(commandNotEmpty, "SvdProcess require non-empty command to execute!")
    require(workDirExists, "SvdProcess working dir must exist! Given: %s".format(workDir))
    require(passACLs, "SvdProcess didn't pass ACL requirements! Failed process: %s".format(command))
    require(pid > 0, "SvdProcess PID always should be > 0!")
    require(ppid > 0, "SvdProcess Parent PID always should be > 0!")
    require(pid != 1, "SvdProcess cannot turn to ZOMBIE!!")
    
    
    // 2011-01-20 02:42:12 - dmilith - TODO: implement SvdProcess requirements
    // require(userListed)
    
    
    /**
     *  @author dmilith
     *
     *   All ACLs must pass for given process
     */
    def passACLs = SvdAccount.aclFor(uid)
    
    
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
     *   Checks uid of user spawning process
     */
    def uidGreaterOrEqualZero =
        uid >= 0

    
    /**
     *  @author dmilith
     *
     *   Checks for dir existance
     */
    def workDirExists =
        new File(workDir).exists
    

    // 2011-06-11 23:53:08 - dmilith - PENDING: fix toString for SvdProcess
    // override def toString =
    //        (
    //        "UID:[%s] " +
    //        "RES:[%s] " +
    //        "SHR:[%s] " +
    //        "PID:[%s] " +
    //        "PPID:[%s] " +
    //        "THREADS:[%s] " +
    //        "PRIO:[%s] " +
    //        "NICE:[%s] " +
    //        "COMMAND:[%s] " +
    //        "TIME_START:[%s] " +
    //        "TIME_KERNEL:[%s] " +
    //        "TIME_TOTAL:[%s] " +
    //        "TIME_USER:[%s] " +
    //        "\n")
    //            .format(uid, rss, shr, pid, ppid, thr, prio, nice, command, timeStart, timeKernel, timeTotal, timeUser)
    
    

}
