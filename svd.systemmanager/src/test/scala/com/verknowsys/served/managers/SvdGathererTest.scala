package com.verknowsys.served.systemmanager.managers


import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.utils._
import com.verknowsys.served.api._

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
 
    }
}
