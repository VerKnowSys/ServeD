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


        "Sigar classes should be predictible" in {
            val a = new ProcState
            val b = new ProcMem
            val c = new ProcCpu
            a must notBeNull
            b must notBeNull
            c must notBeNull
            a.getName must beNull
            println(a)
            println(b)
            println(c)
        }

        
        "/dev/null canWrite should return true" in {
            val cw = new File("/dev/null").canWrite
            cw must beTrue
            
            FileUtils.touch("/tmp/DUPA3")
            val dd = new File("/tmp/DUPA3").canWrite
            dd must beTrue
            
            FileUtils.touch("/tmp/DUPA2")
            val cw2 = new File("/tmp/DUPA2").canWrite
            cw2 must beTrue
        }
        
        
        "Process with empty command cannot be accepted" in {
            isExpectation(
                try {
                	val np = new SvdProcess("")
                	fail("Empty process shouldn't be spawn")
                } catch {
                    case _ =>
                }
            )
        }
                
        
        "Should detect harmful/ incorect commands automatically" in {
            var exploit: SvdProcess = null
            try {
            	exploit = new SvdProcess("dig +trace wp.pl", user = "root", useShell = false)
            } catch {
                case x: Exception =>
                    fail("'Exploit' shouldn't be detected (noShell)! Exception: %s".format(x))
            }
            try {
            	exploit = new SvdProcess("ls", user = "root", useShell = true)
            } catch {
                case x: Exception =>
                    fail("'Exploit' shouldn't be detected (Shell)! Exception: %s".format(x))
            }
            try { 
                exploit = new SvdProcess("dupa", workDir = "/kozaczek.pel")
                fail("Non existant workDir shouldn't be allowed!")
            } catch {
                case _ =>
            }
            try { 
                val a = new SvdProcess("ls -lar", user = "root", useShell = true)
                val b = new SvdProcess("ls -lar", user = "root", useShell = true)
                a must notBeNull
                // 2011-01-24 16:53:16 - dmilith - NOTE: b MAY be null, cause ls without shell may be exceptional case
            } catch {
                case x: Throwable =>
                    fail("Suspicious pattern shouldn't be found!: exc: %s".format(x))
            }
        }
        

        "Root system process should exist on every supported system" in {
            val root = SvdProcess getProcessInfo(1L) // 2011-01-23 16:39:04 - dmilith - NOTE: launchd on mac, init on bsd
            root must notBeNull
            root must beMatching("init|launchd")
            ("PNAME" :: "USER" :: "RES" :: "SHR" :: "PID" :: Nil).foreach{
                elem =>
                    root.toString must beMatching(elem)
            }
            val nonRoot = SvdProcess getProcessInfo(111111111L) // 2011-01-23 16:39:04 - dmilith - NOTE: launchd on mac, init on bsd
            nonRoot must notBeNull
            nonRoot must beEqual("NONE")
        }
        
        
        "SvdSystemProcess must be able to get process list from system" in {
            val pslist = SvdProcess processList(false) // 2011-01-23 17:02:16 - dmilith - NOTE: CHECK: FIXME: it might not be hack at all
            val pslistSorted = SvdProcess processList(sort = true)
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
            val psAmount = SvdProcess processCount(false)
            psAmount must notBeNull
            psAmount must beGreaterThan(10L)
        }
        

        "SvdSystemProcess should throw exception when there's no such process or bad proces pid given" in {
            var a: SvdProcess = null
            try {
                a = new SvdProcess("vrevsaa21kn2###%%%")
            	fail("SvdSystemProcess '%s' with pid -1 was spawned?".format(a))
        	} catch { 
                case x: Throwable =>
                    a = null
            }
            a must beNull
        }


        "process.kill() should behave normally" in {
            val a = SvdProcess kill(123123213, SIGKILL)
            a must beFalse
            val b = SvdProcess kill(-12, SIGINT)
            b must beFalse
        }
        
        
//2011-01-24 19:25:51 - dmilith - TODO: what if some process is spawning a process?

        "it must be able to check that process is alive or not without and with shell" in {
            var a: SvdProcess = null
            var b: SvdProcess = null
            var c: SvdProcess = null
            var d: SvdProcess = null
            try {
                a = new SvdProcess("memcached -u nobody -p 11313", user = "root", useShell = true)
                a must notBeNull
                ("PNAME:" :: "COMMAND:" :: Nil).foreach{
                    elem =>
                        a.toString must beMatching(elem)
                }
                b = new SvdProcess("memcached -u nobody -p 11312", user = "root")
                b must notBeNull
                ("PNAME:" :: "COMMAND:" :: Nil).foreach{
                    elem =>
                        b.toString must beMatching(elem)
                }
            } catch {
                case e: Exception =>
                    fail("Alive isn't working well? Exception: %s, Object: %s".format(e.getMessage, a))
            } finally {
                // 2011-01-24 16:59:05 - dmilith - NOTE: in most cases this will return false: a.alive must beEqual(true)
                // b.alive must beEqual(true)
                a.kill(SIGKILL) must beTrue
                b.kill(SIGKILL) must beTrue
                c = new SvdProcess("echo abc", user = "root")
                d = new SvdProcess("echo abc", user = "root")
                c must notBeNull
                d must notBeNull
                ("PID:" :: "PNAME" :: "COMMAND:" :: Nil).foreach{
                    elem =>
                        c.toString must beMatching(elem)
                        d.toString must beMatching(elem)
                }
            }
        }
        

    } // test should


} // test class
