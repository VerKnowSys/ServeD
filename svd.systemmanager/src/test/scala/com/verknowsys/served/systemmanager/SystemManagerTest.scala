package com.verknowsys.served.systemmanager

import com.verknowsys.served.utils._
import POSIXSignals._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.systemmanager.SvdSystemManager._

import org.hyperic.sigar._
import org.specs._
import java.io._


class SvdSystemManagerTest extends SpecificationWithJUnit with UtilsCommon {


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
            val procStat = internalShell.getProcState(pid)
            println("Proc name: %s, Parent pid: %d, Threads no: %d @ TTY: %s, Priority: %d, Nice: %d".format(procStat.getName, procStat.getPpid, procStat.getThreads, procStat.getTty, procStat.getPriority, procStat.getNice))
            println("CWD of PID: %s, Name of executable: %s".format(procExec.getCwd, procExec.getName))
            
            val cred = internalShell.getProcCredName(pid)
            println("Creds: %s".format(cred.toString))
            
            for (i <- 0 to 5) {
                var a = 1000
                var ps: List[Long] = Nil

                val start = (new java.util.Date).getTime
                while (a > 1) {
                    ps = internalShell.getProcList.toList
                    a -= 1
                }
                val stop = (new java.util.Date).getTime
                println("%d invokes took: %d miliseconds to finish".format(a, (stop-start)))    
                println("ps: %s".format(ps.mkString(", ")))
            }
            
            for (i <- 0 to 5) {
                var a = 1000
                var psDmilith: List[Long] = Nil

                val start = (new java.util.Date).getTime
                while (a > 1) {
                    psDmilith = internalShell.getProcList.filter{ x => internalShell.getProcCredName(x).getUser != "dmilith" }.toList
                    a -= 1
                }
                val stop = (new java.util.Date).getTime
                println("%d invokes took: %d miliseconds to finish".format(a, (stop-start)))    
                println("psDmilith: %s".format(psDmilith.mkString(", ")))
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
        
        
        // "it must be able to get process list from system right away" in {
        //            
        //            @specialized var index, destIndex = 100
        // 
        //            val a = processList(true, true).head
        //            val b = processList(true, false).head
        //            val c = processList(false, true).head
        //            val d = processList(false, false).head
        //            val all = processList(true, true)
        //            
        //            for (i <- 0 to 9) {
        //                index = destIndex
        //                @specialized val start = (new java.util.Date).getTime
        //                while (index > 0) {
        //                    for (i <- a :: b :: c :: d :: Nil) {
        //                        i must notBeNull
        //                        // i.pid.toInt must beGreaterThanOrEqualTo(0)
        //                        i.processName must be matching("[a-zA-Z0-9]*")
        //                    }
        //                    a.getClass.toString must be matching("SystemProcess")
        //                    all.head.getClass.toString must be matching("SystemProcess")
        //                    index -= 1
        //                }
        //                @specialized val stop = (new java.util.Date).getTime
        //                println("TEST: %d. Time elapsed with %d elements: %dms".format(i + 1, destIndex, (stop - start)))
        //            }
        //            
        //            println("TEST: Whole processList size: %d, element count: %d".format(sizeof(all), all.size))
        //            println("TEST: Single value size: %d of class %s".format(sizeof(a), a.getClass))
        //            
        //        }
        //        
        //        
        //        "it should be able to return amount of processes" in {
        //            @specialized var outer, o2 = 10
        //            @specialized val debug = false
        //            @specialized var index, i2 = 25
        //            @specialized val amountNoThreadsAndSorted = processCount(false, true)
        //            if (debug) println("TEST: amountNoThreadsAndSorted: %d".format(amountNoThreadsAndSorted))
        //            @specialized val amountWithThreadsAndSorted = processCount(true, true)
        //            if (debug) println("TEST: amountWithThreadsAndSorted: %d".format(amountWithThreadsAndSorted))
        //        
        //            amountWithThreadsAndSorted must beGreaterThanOrEqualTo(amountNoThreadsAndSorted)
        //            amountNoThreadsAndSorted must beLessThanOrEqualTo(amountWithThreadsAndSorted)
        //        
        //            @specialized val amountNoThreadsNoSorted = processCount(false, false)
        //            if (debug) println("\n\namountNoThreadsNoSorted: %d".format(amountNoThreadsNoSorted))
        //            @specialized val amountWithThreadsNoSorted = processCount(true, false)
        //            if (debug) println("\n\namountWithThreadsNoSorted: %d".format(amountWithThreadsNoSorted))
        //        
        //            @specialized val start = (new java.util.Date).getTime
        //            while (outer >0) {
        //                index = i2
        //                while (index > 0) {
        //                    @specialized val current = processCount(true, true) // 2010-10-24 07:22:03 - dmilith - NOTE: should be heaviest, cause more processes + sorting
        //                    @specialized val current2 = processCount(true, false)
        //                    @specialized val current3 = processCount(false, false)
        //                    @specialized val current4 = processCount(false, true)
        //                    amountNoThreadsAndSorted must beCloseTo(current4, 2) // 2010-10-24 07:24:31 - dmilith - NOTE: error delta +-2
        //                    index -= 1
        //                }
        //                outer -=1
        //            }
        //            @specialized val stop = (new java.util.Date).getTime
        //            println("TEST: Performed processCounts: %d. Result time: %d ms".format(i2*o2*4, (stop - start)))
        //            
        //        }


    } // test should


} // test class
