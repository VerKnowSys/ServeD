package com.verknowsys.served.systemmanager.native


import com.verknowsys.served.testing._
import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._

import org.apache.commons.io.FileUtils
import org.hyperic.sigar._
import org.specs._
import java.lang._
import java.io._


class SvdShellTest extends DefaultTest {


    override def beforeEach {
    }
    
    
    it should "spawn command properly and know when it's dead and throw proper exception when shell is dead" in {
        val sh = new SvdShell(
            new SvdAccount(
                userName = System.getProperty("user.name"),
                uid = randomPort,
                gid = randomPort,
                servicePort = randomPort,
                dbPort = randomPort
            )
        )
        
        sh.exec("")
        sh.dead should be(false)
        sh.close
        sh.dead should be(true)
        evaluating { sh.exec("") } should produce [SvdShellException]
    }
    
    
    it should "get proper output from shell" in {
        import expectj.TimeoutException
        
        val sh = new SvdShell(
            new SvdAccount(
                userName = System.getProperty("user.name"),
                uid = randomPort,
                gid = randomPort,
                servicePort = randomPort,
                dbPort = randomPort
            )
        )
        
        sh.exec(command = "ls -m /dev", expectedStdout = Array("null", "zero"), waitForOutputFor = 2)
        
        evaluating {
            sh.exec(command = "ls -m /dev", expectedStdout = Array("somethingNonExistant"), waitForOutputFor = 1)
        } should produce [TimeoutException]
        
        evaluating {
            sh.exec(command = "ls -m /dev", expectedStderr = Array("somethingNonExistant"), waitForOutputFor = 1)
        } should produce [TimeoutException]
        
        sh.close
    }
    
    
    it should "be able to get return code from ran processes" in {
        val sh = new SvdShell(
            new SvdAccount(
                userName = System.getProperty("user.name"),
                uid = randomPort,
                gid = randomPort,
                servicePort = randomPort,
                dbPort = randomPort
            )
        )
        sh.exec("lsdjf")
        sh.exec("echo $?", expectedStdout = Array("127")) // NOTE: 127 - command not found code from shell
        sh.exec("ls /nonexistantSomethingBlaBla")
        sh.exec("echo $?", expectedStdout = Array("1")) // NOTE: 1 - error thrown from ls command
        sh.exec("ls")
        sh.exec("echo $?", expectedStdout = Array("0"))
        sh.output._2 should include("command not found")
        sh.output._1 should include("0")
        sh.output._1 should include("1")
        sh.output._1 should include("127") // NOTE: beware! output contains output from EVERYTHING ran in particular shell!
        sh.close
    }


}
