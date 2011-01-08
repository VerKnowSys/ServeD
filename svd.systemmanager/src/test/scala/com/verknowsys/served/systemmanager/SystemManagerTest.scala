package com.verknowsys.served.systemmanager

import com.verknowsys.served.utils._
import POSIXSignals._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.systemmanager.SvdSystemManager._

import org.hyperic.sigar._
import org.specs._
import java.io._


class SvdSystemManagerTest extends Specification with UtilsCommon {


    "SystemManager object" should {

        
        doBefore {
        }

        
        "signals defined should have proper integer value" in {

            SIGHUP.id must_== 1
            SIGINT.id must_== 2
            SIGQUIT.id must_== 3
            SIGKILL.id must_== 9

        }
        
        
        "make usage of posixlib functions well" in {

            posixlib.mkdir("/tmp/newdir", 0777)
            (new File("/tmp/newdir")).exists must_== true
            (new File("/tmp/newdir")).isDirectory must_== true
            posixlib.rename("/tmp/newdir", "/tmp/renamedir")
            (new File("/tmp/newdir")).exists must_== false
            (new File("/tmp/renamedir")).exists must_== true
            (new File("/tmp/renamedir")).isDirectory must_== true
            posixlib.chmod("/tmp/renamedir/file1", 0755)

        }
        
        
        "it must be able to get a bunch of system info/ data" in {
            
            val internalShell = new Sigar            
            val psAll = internalShell.getProcList
            
            val pid = internalShell.getPid
            val procExec = internalShell.getProcExe(pid)
            println("CWD of PID: %s, Name of executable: %s".format(procExec.getCwd, procExec.getName))
            
            val procStat = internalShell.getProcState(pid)
            println("Proc name: %s, Parent pid: %d, Threads no: %d @ TTY: %s, Priority: %d, Nice: %d".format(procStat.getName, procStat.getPpid, procStat.getThreads, procStat.getTty, procStat.getPriority, procStat.getNice))
            
            val cred = internalShell.getProcCredName(pid)
            println("Creds: %s".format(cred.toString))
            
            for (i <- 0 to 5) {
                var a = 100

                val start = (new java.util.Date).getTime
                while (a > 1) {
                    internalShell.getProcList
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
                    internalShell.getProcList.filter{ x => internalShell.getProcCredName(x).getUser != "dmilith" }
                    a -= 1
                }
                val stop = (new java.util.Date).getTime
                println("%d invokes of dmilith processes took: %d miliseconds to finish".format(100, (stop-start)))    
                // println("psDmilith: %s".format(psDmilith.mkString(", ")))
            }
            val cred2 = internalShell.getProcCred(pid)
            println("Creds: %s".format(cred2.toString))
            
            val gath: Double = internalShell.getUptime.getUptime
            println("Uptime: %s".format(gath.toString))
            
            val mem = internalShell.getMem
            println("Memory: %s Free, %s Used, %s Total".format(mem.getFree, mem.getUsed, mem.getTotal))
            
            val load = internalShell.getLoadAverage
            println("Load Average: %s / %s / %s".format(load(0), load(1), load(2)))
            
            val disk = internalShell.getDiskUsage("/dev/disk0s2")
            println("Disk usages: %s ".format(disk))
            
            val sysStat = internalShell.getProcStat
            println("Total processes in system: %s".format(sysStat.getTotal))
            println("Process list: %s".format(psAll.mkString(", "))) // no args == show user threads and sort output
            
            for (i <- psAll) {
                val procStat2 = internalShell.getProcState(i)
                println("Proc name: %s, Parent pid: %d, Threads no: %d @ TTY: %s, Priority: %d, Nice: %d".format(procStat2.getName, procStat2.getPpid, procStat2.getThreads, procStat2.getTty, procStat2.getPriority, procStat2.getNice))
                
            }
            
            psAll.size must beGreaterThan(10)
        }
        

    } // test should


} // test class
