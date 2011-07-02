package com.verknowsys.served.systemmanager


import com.verknowsys.served.testing._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import SvdPOSIX._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.systemmanager.native._

import java.io._
import java.lang._


class SvdSystemManagerTest extends DefaultTest with Logging {


    override def beforeEach {
        SvdLowLevelSystemAccess
    }
    

    it should "have proper signal integer values" in {
        import SvdPOSIX._
        
        SIGHUP.id should be(1)
        SIGINT.id should be(2)
        SIGQUIT.id should be(3)
        SIGKILL.id should be(9)
    }
        
        
    it should "be able to get a bunch of system info/ data from sigar" in {
        
        val psAll = SvdLowLevelSystemAccess.getProcList
        psAll should not be (null)
        
        val netstat = SvdLowLevelSystemAccess.netstat
        netstat should not be (null)
        
        val pid = SvdLowLevelSystemAccess.getCurrentProcessPid
        pid should not be ==(null)
        pid should be > (1L)
        
        val procStat = SvdLowLevelSystemAccess.getProcState(pid)
        procStat should not be (null)
        ("Name" :: "State" :: "Ppid" :: "Priority" :: "Nice" :: Nil).foreach{ // "Threads" ::
            elem =>
                procStat.toString should include(elem)
        }
        
        val cred = SvdLowLevelSystemAccess.getProcCredName(pid)
        cred should not be (null)
        ("User" :: "Group" :: Nil).foreach{
            elem =>
                cred.toString should include(elem)
        }
        
        for (i <- 0 to 5) {
            var a = 100
            val start = (new java.util.Date).getTime
            while (a > 1) {
                val r = SvdLowLevelSystemAccess.getProcList
                r should not be (null)
                r.size should be >(3)
                a -= 1
            }
            val stop = (new java.util.Date).getTime
            log.debug("%d invokes took: %d miliseconds to finish".format(100, (stop-start)))    
        }
        
        for (i <- 0 to 5) {
            var a = 100
            val start = (new java.util.Date).getTime
            while (a > 1) {
                val tmp = SvdLowLevelSystemAccess.getProcList.filter{ x =>
                    try {
                    	SvdLowLevelSystemAccess.getProcCredName(x).getUser != "root"
                    } catch { 
                        case _ =>
                            false
                            // this one may fail here when one of spawed threads may be already dead
                    }
                }
                tmp should not be (null)
                tmp.size should be >(3)
                a -= 1
            }
            val stop = (new java.util.Date).getTime
            log.debug("%d invokes of dmilith processes took: %d miliseconds to finish".format(100, (stop-start)))
        }
        val cred2 = SvdLowLevelSystemAccess.getProcCred(pid)
        cred2 should not be (null)
        ("Euid" :: "Gid" :: "Egid" :: "Uid" :: Nil).foreach{
            elem =>
                cred2.toString should include(elem)
                cred2.toString should include("0")
        }
        
        val gath: Double = SvdLowLevelSystemAccess.getSystemUptime
        gath should not be (null)
        
        val mem = SvdLowLevelSystemAccess.mem
        mem should not be (null)
        mem.getFree should be >(1024L*1024L)
        mem.getUsed should be >(1024L*1024L)
        mem.getTotal should be >(1024L*1024L*1024L)
        
        val load = SvdLowLevelSystemAccess.getSystemLoad
        load should not be (null)
        load(0) should be >=(0.0)
        load(1) should be >=(0.0)
        load(2) should be >=(0.0)
        
        val disk = SvdLowLevelSystemAccess.getDiskUsage("/")
        disk should not be (null)
        
        val sysStat = SvdLowLevelSystemAccess.core.get.getProcStat
        sysStat should not be (null)
        sysStat.getTotal should be >=(10L)
        log.debug("Total processes in system: %s".format(sysStat.getTotal))
        
        for (i <- psAll.toList.sortWith{ SvdLowLevelSystemAccess.core.get.getProcState(_).getName < SvdLowLevelSystemAccess.core.get.getProcState(_).getName}) {
            val procstat3 = SvdLowLevelSystemAccess.core.get.getProcState(i)
            procstat3 should not be (null)
            ("Name" :: "State" :: "Ppid" :: "Priority" :: "Nice" :: Nil).foreach{
                elem =>
                    procstat3.toString should include(elem)
            }
        }
        
        psAll.size should be >(10)
    }

}
