// 2011-06-30 22:27:54 - dmilith - PENDING: do SvdAccountManager Test
// package com.verknowsys.served.systemmanager.managers
// 
// 
// import com.verknowsys.served.api._
// import com.verknowsys.served.utils._
// import com.verknowsys.served.systemmanager._
// import com.verknowsys.served.SvdSpecHelpers._
// import org.specs._
// 
// import akka.actor._
// import akka.actor.Actor._
// import akka.testkit.TestKit
// 
// 
// class SvdAccountManagerTest extends Specification with TestKit {
//     
//     import CLibrary._
//     val clib = CLibrary.instance
//     var am: ActorRef = null
//     
//     "SvdAccountManager" should {
//         "respond to GetAccount" in {
//             val account = currentAccount.copy(uid = clib.getuid)
//             am = actorOf(new SvdAccountManager(account)).start
//             am ! GetAccount(account.uid)
//             expectMsg(account)
//             am.stop
//         }
//      }
// }
