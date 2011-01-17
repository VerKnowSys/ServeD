package com.verknowsys.served.systemmanager

import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import POSIX._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.systemmanager.SvdSystemManager._

import org.hyperic.sigar._
import org.specs._
import java.io._
import java.lang._


class SvdSystemManagerTest extends Specification {


    "SystemManager object" should {

        
        doBefore {
        }

        
        "SvdProcess should reply properly to actions and be controllable." in {

            val ps = new SvdProcess("/bin/ls -la")
            val res = ps !? Run // 2011-01-11 01:00:02 - dmilith - NOTE: how to ask, asynchronously waiting for reply
            res must haveClass[Tuple2[String, Integer]]
            val res2 = ps !? "Unrecognized garbage!"
            res2 must beEqual(Ready)

        }

// 2011-01-11 01:11:01 - dmilith - TODO: take care of BLOCKING processes        
        
        "signals defined should have proper integer value" in {

            SIGHUP.id must_== 1
            SIGINT.id must_== 2
            SIGQUIT.id must_== 3
            SIGKILL.id must_== 9

        }
        
        
        "it must be able to get a bunch of system info/ data" in {
            
            val sigarCore = new Sigar            
            val psAll = sigarCore.getProcList
            
            val pid = sigarCore.getPid
            // val procExec = sigarCore.getProcExe(pid)
            // println("CWD of PID: %s, Name of executable: %s".format(procExec.getCwd, procExec.getName))
            
            val procStat = sigarCore.getProcState(pid)
            println("Proc name: %s, Parent pid: %d, Threads no: %d @ TTY: %s, Priority: %d, Nice: %d".format(procStat.getName, procStat.getPpid, procStat.getThreads, procStat.getTty, procStat.getPriority, procStat.getNice))
            
            val cred = sigarCore.getProcCredName(pid)
            println("Creds: %s".format(cred.toString))
            
            for (i <- 0 to 5) {
                var a = 100

                val start = (new java.util.Date).getTime
                while (a > 1) {
                    sigarCore.getProcList
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
                    sigarCore.getProcList.filter{ x => sigarCore.getProcCredName(x).getUser != "dmilith" }
                    a -= 1
                }
                val stop = (new java.util.Date).getTime
                println("%d invokes of dmilith processes took: %d miliseconds to finish".format(100, (stop-start)))    
                // println("psDmilith: %s".format(psDmilith.mkString(", ")))
            }
            val cred2 = sigarCore.getProcCred(pid)
            println("Creds: %s".format(cred2.toString))
            
            val gath: Double = sigarCore.getUptime.getUptime
            println("Uptime: %s".format(gath.toString))
            
            val mem = sigarCore.getMem
            println("Memory: %s Free, %s Used, %s Total".format(mem.getFree, mem.getUsed, mem.getTotal))
            
            val load = sigarCore.getLoadAverage
            println("Load Average: %s / %s / %s".format(load(0), load(1), load(2)))
            
            val disk = sigarCore.getDiskUsage("/")
            println("Disk usages: %s ".format(disk))
            
            val sysStat = sigarCore.getProcStat
            println("Total processes in system: %s".format(sysStat.getTotal))
            println("Process list: %s".format(psAll.mkString(", "))) // no args == show user threads and sort output
            
            for (i <- psAll.toList.sortWith{ sigarCore.getProcState(_).getName < sigarCore.getProcState(_).getName}) {
                val procstat3 = sigarCore.getProcState(i)
                println("PS alphabetic: %s".format(procstat3))
            }
            
            psAll.size must beGreaterThan(10)
        }
        

    } // test should


} // test class
