// package com.verknowsys.served.maintainer
// 
// import akka.actor.Actor
// import akka.util.Logging
// 
// import com.verknowsys.served.utils.signals._
// import com.verknowsys.served.api._
// 
// 
// class SvdApiSession extends Actor {
//     import self._
//     
//     var manager: Option[SvdAccountSvdManager] = None
//     
//     def receive = {
//         case General.Connect(username) => 
//             // XXX: This sucks, it really should be async
//             (SvdAccountsManager !? SvdAccountSvdManager.CheckUser(username)) match {
//                 case Some(m) =>
//                     manager = Some(m)
//                     reply(Success)
//                 case None => 
//                     reply(Error("User with name '%' not found" % username))
//             }
//                     
//         case _ => reply(Success)
//     }
// }
// 
// object SvdApiServer {
//     final val host= "localhost"
//     final val port = 5555
//     
//     def start {
//         Actor.remote.start(host, port)
//         Actor.remote.registerPerSession("service:api", Actor.actorOf[ApiSession])
//     }
// }
