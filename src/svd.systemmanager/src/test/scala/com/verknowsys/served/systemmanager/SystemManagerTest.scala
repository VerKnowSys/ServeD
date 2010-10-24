package com.verknowsys.served.systemmanager

import com.verknowsys.served.utils._
import POSIXSignals._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.systemmanager.SvdSystemManager._

import org.specs._
import java.io._


class SvdSystemManagerTest extends SpecificationWithJUnit with UtilsCommon {

    "SystemManager object" should {

        
        // doBefore {
        // }

        
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
        
        
        "it must be able to check list of pids of running system and also match for name" in {
            
            val pss = pstreelib.processes(1,1)
            // println("TEST: User processes: %s".format(pss))
            processList() must notBe(List())
            pss.mkString.length must beGreaterThan(5) // greater than strlen("NONE") + 1

        }
        
        
        "it must pass basic efficiency benchmark to be usefull (it's native so it should be blazing fast right?)" in {
            
            @specialized var index = 100

            while (index > 0) {
                
                val a = processList(true, true).head
                val b = processList(true, false).head
                val c = processList(false, true).head
                val d = processList(false, false).head
                
                for (i <- a :: b :: c :: d :: Nil) {
                    
                    i must notBeNull
                    // i.pid must beGreaterThan(-1) // 2010-10-24 06:40:21 - dmilith - NOTE: (root,0)
                    i.processName must be matching("[a-zA-Z0-9]*")
                    
                }
                
                index -= 1
            }
            
        }
        
        
        "it should be able to return amount of processes" in {
            @specialized var outer = 10
            @specialized val debug = false
            
            @specialized var index = 25
        
            @specialized val amountNoThreadsAndSorted = processCount(false, true)
            if (debug) println("TEST: amountNoThreadsAndSorted: %d".format(amountNoThreadsAndSorted))
        
            @specialized val amountWithThreadsAndSorted = processCount(true, true)
            if (debug) println("TEST: amountWithThreadsAndSorted: %d".format(amountWithThreadsAndSorted))
        
            amountWithThreadsAndSorted must beGreaterThanOrEqualTo(amountNoThreadsAndSorted)
            amountNoThreadsAndSorted must beLessThanOrEqualTo(amountWithThreadsAndSorted)
        
            @specialized val amountNoThreadsNoSorted = processCount(false, false)
            if (debug) println("\n\namountNoThreadsNoSorted: %d".format(amountNoThreadsNoSorted))
        
            @specialized val amountWithThreadsNoSorted = processCount(true, false)
            if (debug) println("\n\namountWithThreadsNoSorted: %d".format(amountWithThreadsNoSorted))
        
            while (outer >0) {
            
                @specialized val start = (new java.util.Date).getTime
                while (index > 0) {
                    @specialized val current = processCount(true, true) // 2010-10-24 07:22:03 - dmilith - NOTE: should be heaviest, cause more processes + sorting
                    @specialized val current2 = processCount(true, false)
                    @specialized val current3 = processCount(false, false)
                    @specialized val current4 = processCount(false, true)
                    amountNoThreadsAndSorted must beCloseTo(current4, 2) // 2010-10-24 07:24:31 - dmilith - NOTE: error delta +-10
                    index -= 1
                }
                @specialized val stop = (new java.util.Date).getTime
                println("TEST: Count size result time: " + (stop - start) + "ms.")
                
                if (outer % 10 == 0) {
                    println("TEST: %s".format(processList(true, true)))
                }
                
                outer -=1
            }
        }


    } // test should


} // test class
