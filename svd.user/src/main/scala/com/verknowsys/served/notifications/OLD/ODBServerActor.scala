/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

// // This Software is a close code project. You may not redistribute this code without permission of author.
// 
// package com.verknowsys.served.gitbotnotifier
// 
// 
// 
// import com.verknowsys.served.utils.signals.{Init, Quit}
// import com.verknowsys.served.utils.props.Preferences
// 
// import org.neodatis.odb.{OdbSvdConfiguration, ODBServer, ODBFactory}
// import org.apache.log4j.Logger
// import scala.actors._
// 
// 
// object ODBServerActor extends Actor {
//  
//  private val props = SvdBot.props
//  private val logger = Logger.getLogger(ODBServerActor.getClass)
//  private val debug = props("debug")
//  private val absolutePathToBotODBDir = System.getProperty("user.home") + "/" + ".svd/"
//  private var server: ODBServer = null
// 
//  def initServer = {
//    try {
//      OdbSvdConfiguration.setAutomaticCloseFileOnExit(true)
//      OdbSvdConfiguration.setDatabaseCharacterEncoding( "UTF8" )
//      server = ODBFactory.openServer(props("databaseODBPort"))
//      server.addBase(props("xmppDatabaseName"), absolutePathToBotODBDir + props("xmppDatabaseFileName"))
//      server.addBase(props("ircDatabaseName"), absolutePathToBotODBDir + props("ircDatabaseFileName"))
//      server.startServer(false) //start server in current thread
//    } catch {
//      case x: Throwable => {
//        info("### Error: exception occured in ODBServerActor!")
//        debug( x.printStackTrace )
//      }
//    } finally {
//      if (server != null) {
//        server.close
//      }
//    }
//  }
//  
//    override def act = {
//    Actor.loop {
//      react {
//        case Init => {
//          initServer
//          act
//        }
//        case Quit => {
//          debug("*** ODBServer received Quit command.")
//          if (server != null) server.close
//          exit
//        }
//      }
//    }
//  } 
// }
