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
    
    
    it should "spawn command properly and know when it's dead" in {
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
    }
    
    
    it should "get proper output from shell" in {
        val sh = new SvdShell(
            new SvdAccount(
                userName = System.getProperty("user.name"),
                uid = randomPort,
                gid = randomPort,
                servicePort = randomPort,
                dbPort = randomPort
            )
        )
        
        sh.exec("ls /dev")
        sh.output._2 should be("")
        sh.output._1 should include("null")
        sh.output._1 should include("zero")
        sh.close
    }


}
