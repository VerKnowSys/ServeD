package com.verknowsys.served.systemmanager


import POSIXSignals._

import org.specs._
import java.io._


class SystemManagerTest extends SpecificationWithJUnit {

    "SystemManager object" should {
        
        // doBefore {doSomething}
        
        "signals defined should have proper integer value" in {
            SIGHUP.id must_== 1
            SIGINT.id must_== 2
            SIGQUIT.id must_== 3
            SIGKILL.id must_== 9
        }
        
        
        "make usage of posix functions well" in {
            SystemManager.posix.mkdir("/tmp/newdir", 0777)
            (new File("/tmp/newdir")).exists must_== true
            (new File("/tmp/newdir")).isDirectory must_== true
            SystemManager.posix.rename("/tmp/newdir", "/tmp/renamedir")
            (new File("/tmp/newdir")).exists must_== false
            (new File("/tmp/renamedir")).exists must_== true
            (new File("/tmp/renamedir")).isDirectory must_== true
            SystemManager.posix.chmod("/tmp/renamedir/file1", 0755)
        }
    }
            
}
