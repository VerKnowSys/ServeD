package com.verknowsys.served.managers

import com.verknowsys.served.SpecHelpers._
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.maintainer.Account
import org.specs._

import akka.actor._
import akka.actor.Actor._

class AccountManagerTest extends Specification with ExpectActorSpecification {
    var am: ActorRef = null
    
    "AccountManager" should {
        doBefore { 
            beforeExpectActor
        }
        
        doAfter { 
            afterExpectActor
            registry.shutdownAll 
        }
        
        "respond to GetAccount" in {
            val account = new Account(userName = "teamon")
            am = actorOf(new AccountManager(account)).start
            am ! GetAccount
            expectActor ? account
        }
     }
    
}