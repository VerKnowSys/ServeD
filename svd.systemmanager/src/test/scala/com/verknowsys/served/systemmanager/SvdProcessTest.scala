package com.verknowsys.served.systemmanager

import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import SvdPOSIX._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager._

import org.hyperic.sigar._
import org.specs._
import java.io._
import java.lang._
import org.apache.commons.io.FileUtils


// 2011-01-22 17:57:49 - dmilith - TODO: extend tests for some critical moments: segvs, ooms and similar app behaviour

class SvdProcessTest extends Specification {


    "SvdProcessTest" should {
        
        doAfter {
        }
        
        doBefore {
        }
        
        
        "Should detect harmful/ incorect commands automatically" in {
            var exploit: SvdProcess = null
            try {
            	exploit = new SvdProcess("ls", user = "root")
            } catch {
                case _ =>
                    fail("'Exploit' shouldn't be detected")
            }
            try { 
                exploit = new SvdProcess("dupa", workDir = "/kozaczek.pel")
                fail("Non existant workDir shouldn't be allowed!")
            } catch {
                case _ =>
            }
            try { 
                exploit = new SvdProcess("ls -lar /", user = "root")
                exploit must notBeNull
            } catch {
                case _ =>
                    fail("Suspicious pattern shouldn't be found!")
            }
        }
        

        "Root system process should exist on every supported system" in {
            val root = new SvdSystemProcess(1L) // 2011-01-23 16:39:04 - dmilith - NOTE: launchd on mac, init on bsd
            root must notBeNull
            ("PNAME" :: "USER" :: "RES" :: "SHR" :: "PID" :: Nil).foreach{
                elem =>
                    root.toString must beMatching(elem)
            }
        }
        
        
        "SvdSystemProcess must be able to get process list from system" in {
            val pslist = new SvdSystemProcess(1L) processList(false) // 2011-01-23 17:02:16 - dmilith - NOTE: CHECK: FIXME: it might not be hack at all
            val pslistSorted = new SvdSystemProcess(1L) processList(sort = true)
            pslist must notBeNull
            pslistSorted must notBeNull
            println("PLIST: " + pslist)
            println("PLIST-S: " + pslistSorted)
            if (pslistSorted == pslist)
                fail("Sorted and Unsorted process list, never should be equal!")
            pslist.size must beGreaterThan(10L)
            pslistSorted.size must beGreaterThan(10L)
        }

        
        "SvdSystemProcess must be able to get amount of processes in system" in {
            val psAmount = new SvdSystemProcess(1L) processCount(false)
            psAmount must notBeNull
            psAmount must beGreaterThan(10L)
        }
        

        "SvdSystemProcess should throw exception when there's no such process or bad proces pid given" in {
            var a: SvdSystemProcess = null
            try {
                a = new SvdSystemProcess(-1L)
            	fail("SvdSystemProcess '%s' with pid -1 was spawned?".format(a))
        	} catch { 
                case x: Any =>
                    a = null
            }
            try {
                a = new SvdSystemProcess(433434343L)
            	fail("SvdSystemProcess '%s' with pid 433434343L was spawned?".format(a))
        	} catch { 
                case x: Any =>
                    a = null
            }
            a must beNull
        }


        "SvdSystemProcess should return object with information about process when querying system process" in {
            val a = new SvdSystemProcess(1L) // usually exists in SvdPOSIX system as launchd/init
            a.pid must beEqual(1L)
            a.name must beMatching("init|launchd")
            ("PNAME" :: "USER" :: "RES" :: "SHR" :: "PID" :: Nil).foreach{
                elem =>
                    a.toString must beMatching(elem)
            }
        }

        
        "it must be able to run ls process properly using default PATH settings" in {
            var a: SvdProcess = null
            try {
                synchronized {
                    a = new SvdProcess("ls", outputRedirectDestination = "/tmp/served_ls_abc", useShell = false, user = "root")
                    a must notBeNull
                    a = null
                }
            } catch { 
                case x: Any =>
                    fail("Problem with spawing process %s. Exception: %s".format(a, x))
            }
            try { 
                new SvdSystemProcess(a.pid)
                fail("SvdProcess pid should be non existant")
            } catch {
                case _ =>
            }
            a must beNull
        }


        // 2011-01-22 18:02:08 - dmilith - TODO: add sam test with shell
        "it must be able to check that process is alive or not without shell" in {
            var a: SvdProcess = null
            var b: SvdProcess = null
                try {
                    synchronized {
                        a = new SvdProcess("memcached -u nobody", user = "root", useShell = false, outputRedirectDestination = "/tmp/served_memcached")
                        a must notBeNull
                        ("pid:" :: "cmdSvdProc:" :: Nil).foreach{
                            elem =>
                                a.toString must beMatching(elem)
                        }
                    }
                } catch {
                    case e: Exception =>
                        fail("Alive isn't working well? Exception: %s, Object: %s".format(e.getMessage, a))
                }
                synchronized {
                    b = new SvdProcess("kill %d".format(a.pid), user = "root", useShell = false, outputRedirectDestination = "/tmp/served_kill")
                    b must notBeNull
                    ("pid:" :: "cmdSvdProc:" :: Nil).foreach{
                        elem =>
                            b.toString must beMatching(elem)
                    }
                }
        }

    } // test should


} // test class
