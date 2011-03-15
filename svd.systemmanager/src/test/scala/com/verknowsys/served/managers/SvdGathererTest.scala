package com.verknowsys.served.systemmanager.managers


import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.utils.signals.SvdPOSIX._
import com.verknowsys.served.utils._
import com.verknowsys.served.api._
import com.verknowsys.served.db._
import com.verknowsys.served._

import java.io._
import java.util.{Calendar, GregorianCalendar}
import akka.actor.Actor.{actorOf, registry}
import akka.actor.ActorRef
import org.specs._


class SvdGathererTest extends Specification with SvdExpectActorSpecification {

    val homeDir1 = testPath("home/teamon")
    val homeDir2 = testPath("home/dmilith")
    
    val account1 = new SvdAccount(userName = "teamon", homeDir = homeDir1)
    val account2 = new SvdAccount(userName = "dmilith", homeDir = homeDir2)
    
    var gather1: ActorRef = null
    var gather2: ActorRef = null
    
    
    "SvdGatherer" should {
        doBefore {
            beforeExpectActor
            gather1 = actorOf(new SvdGatherer(account1)).start
            gather2 = actorOf(new SvdGatherer(account2)).start
            mkdir(homeDir1)
            mkdir(homeDir2)
        }
        
        doAfter {
            afterExpectActor
            registry.shutdownAll
            rmdir(homeDir1)
            rmdir(homeDir2)
        }
        
        "create more than one instance of SvdGatherer" in {
            gather1 ! "Test signal 1"
            expectActor ? Nil
            afterExpectActor
            beforeExpectActor
            gather2 ! "Test signal 2"
            expectActor ? Nil
        }
 
        "Calendar should give correct values" in {
            val calendar0 = new GregorianCalendar(0,0,0,0,0,0)
            calendar0.get(Calendar.HOUR) must beEqual(0)
            calendar0.get(Calendar.MINUTE) must beEqual(0)
            calendar0.get(Calendar.SECOND) must beEqual(0)
            
            val calendar1 = new GregorianCalendar(0,0,0,0,0,0)
            calendar1.set(Calendar.SECOND, 3666)
            calendar1.get(Calendar.HOUR) must beEqual(1)
            calendar1.get(Calendar.MINUTE) must beEqual(1)
            calendar1.get(Calendar.SECOND) must beEqual(6)
            
            val calendar2 = new GregorianCalendar(0,0,0,0,0,0)
            calendar2.set(Calendar.SECOND, 3667)
            calendar2.get(Calendar.HOUR) must beEqual(1)
            calendar2.get(Calendar.MINUTE) must beEqual(1)
            calendar2.get(Calendar.SECOND) must beEqual(7)
        }
        
        "SvdUtils.secondsToHMS() should give correct values" in {
            val matcher = SvdUtils.secondsToHMS(3666)
            matcher must beMatching("01h:01m:06s")
            val matcher2 = SvdUtils.secondsToHMS(3667L.toInt)
            matcher2 must beMatching("01h:01m:07s")
        }
        
        "we should be able to check when it's worth to compress String" in {
            val in = new BufferedReader(new FileReader("/dev/urandom"))
            
            val str = new StringBuilder("")
            println(SvdUtils.bench {
                for (i <- 1.to(1500)) {
                    str.append(in.read)
                }    
            })
            
            val chpoint = str.toString
            // println("str: %s".format(chpoint))
            val chplen = chpoint.length
            val complen = SvdUtils.compress(chpoint).length
            val decomplen = SvdUtils.decompress(SvdUtils.compress(chpoint)).length
            println("chpoint (length): %d".format(chplen))
            println("chpoint (compress): %d".format(complen))
            println("chpoint (decompress): %d".format(decomplen))
            for (i <- 1.to(500000)) {
                str.append(in.read)
            }
            println("Will compress String of length: %d".format(str.toString.length))
            println("####################################################################\n" +
                SvdUtils.bench {
                    SvdUtils.decompress(SvdUtils.compress(str.toString))
                }
            )
            in.close
            chplen must beEqual(decomplen)
            chplen must beGreaterThan(complen)
        }

    }
}
