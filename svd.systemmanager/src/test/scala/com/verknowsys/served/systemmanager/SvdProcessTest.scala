package com.verknowsys.served.systemmanager

import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import POSIX._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.SvdSystemManager._

import org.hyperic.sigar._
import org.specs._
import java.io._
import java.lang._


class SvdProcessTest extends Specification {


    "SvdProcessTest" should {
        
        doAfter {
        }


        "SystemProcess should throw exception when there's no such process or bad proces pid given" in {
            var a: SystemProcess = null
            try {
                a = new SystemProcess(-1L)
            	fail("SystemProcess '%s' with pid -1 was spawned?".format(a))
        	} catch { 
                case x: Any =>
                    a = null
            }
            try {
                a = new SystemProcess(433434343L)
            	fail("SystemProcess '%s' with pid 433434343L was spawned?".format(a))
        	} catch { 
                case x: Any =>
                    a = null
            }
            a must beNull
        }


        "SystemProcess should return object with information about process when querying system process" in {
            val a = new SystemProcess(1L) // usually exists in POSIX system as launchd/init
            a.pid must beGreaterThan(0L)
            a.name.size must beGreaterThan(0L)
            ("PNAME" :: "USER" :: "RES" :: "SHR" :: "PID" :: Nil).foreach{
                elem =>
                    a.toString must beMatching(elem)
            }
        }

        
        "it must be able to run df process properly using default PATH settings" in {
            var a: SvdProcess = null
            try {
                synchronized {
                    a = new SvdProcess("df", outputRedirectDestination = "/tmp/served_DUPA", useShell = true, user = "root")
                    Thread.sleep(500)
                    a.alive must be(false)
                }
                a = null
            } catch { 
                case x: Any =>
                    fail("Exception occured: " + x)
            }
            try { 
                new SystemProcess(a.pid)
                fail("SvdProcess pid should be non existant")
            } catch {
                case _ =>
            }
            a must beNull
        }


        "it must be able to check that process is alive or not" in {
            var a: SvdProcess = null
            try {
                synchronized {
                    a = new SvdProcess("memcached", useShell = false)
                    Thread.sleep(500)
                    a.alive must be(true)
                }
            } catch {
                case e: Exception =>
                    fail("Alive isn't working well? It's %s".format(a.alive))
            }
            // 2011-01-20 11:00:04 - dmilith - hacky: kill memcached after test pass
            new SvdProcess("kill %d".format(a.pid), user = "root")
        }

    } // test should


} // test class
