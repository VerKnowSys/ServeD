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



// 2011-01-22 17:57:49 - dmilith - TODO: extend tests for some critical moments: segvs, ooms and similar app behaviour

class SvdProcessTest extends Specification {


    "SvdProcessTest" should {
        
        doAfter {
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
            a.pid must beGreaterThan(0L)
            a.name.size must beGreaterThan(0L)
            ("PNAME" :: "USER" :: "RES" :: "SHR" :: "PID" :: Nil).foreach{
                elem =>
                    a.toString must beMatching(elem)
            }
        }

        
        "it must be able to run df process properly using default PATH settings" in {
            var a: SvdProcess = null
            synchronized {
                try {
                    a = new SvdProcess("df", outputRedirectDestination = "/tmp/served_df_abc", useShell = true, user = "root")
                    Thread.sleep(500)
                    a.alive must be(false)
                    a = null
                } catch { 
                    case x: Any =>
                        fail("Exception occured: " + x)
                }
                try { 
                    new SvdSystemProcess(a.pid)
                    fail("SvdProcess pid should be non existant")
                } catch {
                    case _ =>
                }
                a must beNull
            }
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
