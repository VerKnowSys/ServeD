package com.verknowsys.served.systemmanager

import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import SvdPOSIX._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.systemmanager.native._

import org.hyperic.sigar._
import org.specs._
import java.io._
import java.lang._


class SvdSystemManagerTest extends Specification {


    "SvdSystemManager object" should {

        
        doBefore {
        }

        
        "signals defined should have proper integer value" in {
            import SvdPOSIX._
            
            SIGHUP.id must_== 1
            SIGINT.id must_== 2
            SIGQUIT.id must_== 3
            SIGKILL.id must_== 9

        }
        
        
        "it must be able to get a bunch of system info/ data from sigar" in {
            
            val psAll = SvdLowLevelSystemAccess.core.get.getProcList
            psAll must notBeNull
            
            val pid = SvdLowLevelSystemAccess.core.get.getPid
            pid must notBeNull
            pid must haveClass[Long]
            pid must beGreaterThan(0L)
            // val procExec = SvdLowLevelSystemAccess.core.get.getProcExe(pid)
            // println("CWD of PID: %s, Name of executable: %s".format(procExec.getCwd, procExec.getName))
            
            val procStat = SvdLowLevelSystemAccess.core.get.getProcState(pid)
            procStat must notBeNull
            ("Name" :: "State" :: "Ppid" :: "Priority" :: "Nice" :: Nil).foreach{ // "Threads" ::
                elem =>
                    procStat.toString must beMatching(elem)
            }
            // println("Proc name: %s, Parent pid: %d, Threads no: %d @ TTY: %s, Priority: %d, Nice: %d".format(procStat.getName, procStat.getPpid, procStat.getThreads, procStat.getTty, procStat.getPriority, procStat.getNice))
            
            val cred = SvdLowLevelSystemAccess.core.get.getProcCredName(pid)
            cred must notBeNull
            ("User" :: "Group" :: Nil).foreach{
                elem =>
                    cred.toString must beMatching(elem)
            }
            // println("Creds: %s".format(cred.toString))
            
            for (i <- 0 to 5) {
                var a = 100

                val start = (new java.util.Date).getTime
                while (a > 1) {
                    val r = SvdLowLevelSystemAccess.core.get.getProcList
                    r must notBeNull
                    r.size must beGreaterThan(3)
                    a -= 1
                }
                val stop = (new java.util.Date).getTime
                println("%d invokes took: %d miliseconds to finish".format(100, (stop-start)))    
                // println("ps: %s".format(ps.mkString(", ")))
            }
            
            for (i <- 0 to 5) {
                var a = 100

                val start = (new java.util.Date).getTime
                while (a > 1) {
                    val tmp = SvdLowLevelSystemAccess.core.get.getProcList.filter{ x =>
                        try {
                        	SvdLowLevelSystemAccess.core.get.getProcCredName(x).getUser != "root"
                        } catch { 
                            case _ =>
                                false
                                // this one may fail here when one of spawed threads may be already dead
                        }
                    }
                    tmp must notBeNull
                    tmp.size must beGreaterThan(3)
                    a -= 1
                }
                val stop = (new java.util.Date).getTime
                println("%d invokes of dmilith processes took: %d miliseconds to finish".format(100, (stop-start)))
                // println("psDmilith: %s".format(psDmilith.mkString(", ")))
            }
            val cred2 = SvdLowLevelSystemAccess.core.get.getProcCred(pid)
            cred2 must notBeNull
            ("Euid" :: "Gid" :: "Egid" :: "Uid" :: Nil).foreach{
                elem =>
                    cred2.toString must beMatching(elem)
                    cred2.toString must beMatching("0")
            }
            // println("Creds: %s".format(cred2.toString))
            
            val gath: Double = SvdLowLevelSystemAccess.core.get.getUptime.getUptime
            gath must notBeNull
            gath must haveClass[Double]
            // gath must beGreaterThanOrEqualTo(0.0)
            // println("Uptime: %s".format(gath.toString))
            
            val mem = SvdLowLevelSystemAccess.core.get.getMem
            mem must notBeNull
            mem.getFree must beGreaterThan(1024L*1024L)
            mem.getUsed must beGreaterThan(1024L*1024L)
            mem.getTotal must beGreaterThan(1024L*1024L*1024L) // 2011-01-20 13:58:59 - dmilith - NOTE: minimum of 1GiB RAM on served machine is expected
            // println("Memory: %s Free, %s Used, %s Total".format(mem.getFree, mem.getUsed, mem.getTotal))
            
            val load = SvdLowLevelSystemAccess.core.get.getLoadAverage
            load must notBeNull
            load(0) must beGreaterThanOrEqualTo(0.0)
            load(1) must beGreaterThanOrEqualTo(0.0)
            load(2) must beGreaterThanOrEqualTo(0.0)
            // println("Load Average: %s / %s / %s".format(load(0), load(1), load(2)))
            
            val disk = SvdLowLevelSystemAccess.core.get.getDiskUsage("/")
            disk must notBeNull
            // println("Disk usages: %s ".format(disk))
            
            val sysStat = SvdLowLevelSystemAccess.core.get.getProcStat
            sysStat must notBeNull
            sysStat.getTotal must beGreaterThanOrEqualTo(10L)
            println("Total processes in system: %s".format(sysStat.getTotal))
            // println("Process list: %s".format(psAll.mkString(", "))) // no args == show user threads and sort output
            
            for (i <- psAll.toList.sortWith{ SvdLowLevelSystemAccess.core.get.getProcState(_).getName < SvdLowLevelSystemAccess.core.get.getProcState(_).getName}) {
                val procstat3 = SvdLowLevelSystemAccess.core.get.getProcState(i)
                procstat3 must notBeNull
                ("Name" :: "State" :: "Ppid" :: "Priority" :: "Nice" :: Nil).foreach{
                    elem =>
                        procstat3.toString must beMatching(elem)
                }
                // println("PS alphabetic: %s".format(procstat3))
            }
            
            psAll.size must beGreaterThan(10)
        }
        

    } // test should


} // test class
