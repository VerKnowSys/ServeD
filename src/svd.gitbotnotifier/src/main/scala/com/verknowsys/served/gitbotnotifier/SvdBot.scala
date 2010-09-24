// // © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// // This Software is a close code project. You may not redistribute this code without permission of author.
// 
// package com.verknowsys.served.gitbotnotifier
// 
// 
// import com.verknowsys.served.utils.Utils
// import com.verknowsys.served.utils.signals.{ProcessMessages, MainLoop, Init, Quit}
// 
// import java.io.OutputStreamWriter
// import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
// import scala.actors._
// 
// 
// object SvdBot extends Actor with Utils {
// 
//  addShutdownHook {
//    XMPPActor ! Quit
//     // ODBServerActor ! Quit
// //   IRCActor ! Quit
//    SvdBot ! Quit
//    logger.info("Done\n")
//  }
// 
//   // NOTE: when in standalone mode:
//  def main(args: Array[String]) {
//    setLoggerLevelDebug(if (props.bool("debug") getOrElse true) Level.TRACE else Level.INFO)
//    logger.info("User home dir: " + System.getProperty("user.home"))
//    logger.debug("Params: " + args + ". Params size: " + args.length)
//    this.start
//  }
//  
//  override def act = {
//     // ODBServerActor.start
//     // ODBServerActor ! Init
//    XMPPActor.start
//    XMPPActor ! Init
// //   IRCActor.start
//    
//    react {
//      case MainLoop => {
//        Actor.loop {
//          Thread sleep 500 // 500 ms for each check. That's enough even for very often updated repository
//          XMPPActor ! ProcessMessages
//        }
//      }
//      case Quit => {
//        exit
//      }
//    }
//    logger.info("Ready to serve. waiting for orders.")
//  }
//  
// }