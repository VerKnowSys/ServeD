package com.verknowsys.served.systemmanager.managers


import com.verknowsys.served.systemmanager._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.systemmanager.native.SvdAccount
import org.specs._

import akka.actor._
import akka.actor.Actor._


class SvdAccountManagerTest extends Specification with SvdExpectActorSpecification {

    var am: ActorRef = null
    
    "SvdAccountManager" should {
        doBefore { 
            beforeExpectActor
        }
        
        doAfter { 
            afterExpectActor
            registry.shutdownAll 
        }
        
        "respond to GetAccount" in {
            val account = new SvdAccount(userName = "teamon")
            am = actorOf(new SvdAccountManager(account)).start
            am ! GetAccount
            expectActor ? account
        }
     }
    
}