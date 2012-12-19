// package com.verknowsys.served.maintainer

// import akka.dispatch._
// import akka.pattern.ask
// import akka.remote._
// import akka.util.Duration
// import akka.util.Timeout
// import akka.util.duration._
// import akka.actor._

// import com.verknowsys.served._
// import com.verknowsys.served.api._
// import com.verknowsys.served.utils._
// import com.verknowsys.served.managers._



// class SvdApiSession extends SvdManager {
//     log.info("Starting new API session")

//     private var manager: Option[ActorRef] = None // XXX: Var

//     override def receive = {
//         case General.GetStatus =>
//             sender ! General.Status.Disconnected

//         case General.Connect(userUid) =>
//             log.trace("Remote client trying to connect with UID: %s", userUid)
//             val man = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort))
//             (man ? Admin.GetAccountManager(userUid)) onSuccess {
//                 case Some(x: SvdAccountsManager) =>
//                     context.become(dispatch)
//                     log.info("Remote client successfully connected with UID: %s", userUid)
//                     sender ! Success

//                 case None =>
//                     sender ! Error("No remote client found")

//             } onFailure {
//                 case x =>
//                     sender ! Error("Failure linking to remote client, cause of %s".format(x))
//             }

//     }

//     // lazy val loggingManagers = remote.actorFor("service:logging-manager", "localhost", 8000) :: Nil  // XXX: HACK: should use account.servicePort instead of 8000

//     protected def dispatch: Receive = traceReceive {
//         case General.GetStatus =>
//             sender ! General.Status.Connected

//         case msg: Logger.Base =>
//             log.debug("Remote client sent %s. Forwarding to LoggingManager", msg)

//             // temporary!
//             // loggingManagers.foreach(_ ! msg) // disabled due to 8000 port issue

//             context.actorOf(Props[LoggingManager]) //.foreach { _ forward msg }
//             // XXX: CHECKME

//         case msg: Admin.Base =>
//             log.debug("Remote client sent %s. Forwarding to SvdSystemInfo", msg)
//             sender ! context.actorOf(Props[SvdSystemInfo]) //.foreach { _ forward msg }

//         case msg if manager.isDefined =>
//             log.debug("Remote client sent %s. Forwarding to AccountManager (%s)", msg, manager)
//             manager.foreach { _ forward msg }
//     }
// }

