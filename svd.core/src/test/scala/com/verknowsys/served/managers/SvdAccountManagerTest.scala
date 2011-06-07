package com.verknowsys.served.systemmanager.managers


import com.verknowsys.served.systemmanager._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.systemmanager.native.SvdAccount
import org.specs._

import akka.actor._
import akka.actor.Actor._
import akka.testkit.TestKit


class SvdAccountManagerTest extends Specification with TestKit {

    var am: ActorRef = null
    
    "SvdAccountManager" should {
        "respond to GetAccount" in {
            val account = new SvdAccount(userName = "teamon")
            am = actorOf(new SvdAccountManager(account)).start
            am ! GetAccount
            expectMsg(account)
        }
     }
}
