// // © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// // This Software is a close code project. You may not redistribute this code without permission of author.
// 
// package com.verknowsys.served.gitbotnotifier
// 
// 
// // import com.verknowsys.served.utils.commiter.{Commit, DbAddCommit}
// import com.verknowsys.served.utils.signals.{Init, Quit, ProcessMessages, MainLoop}
// import com.verknowsys.served.utils.command.exec.CommandExec
// 
// import org.apache.log4j.Logger
// import scala.actors._
// import org.neodatis.odb._
// import org.neodatis.odb.impl.core.query.criteria._
// import org.neodatis.odb.core.query.criteria._
// import org.jivesoftware.smack._
// import org.jivesoftware.smack.packet._
// import org.jivesoftware.smack.filter._
// 
// 
// // TODO: to be refactored - Preferences should be parametrized
// object XMPPActor extends Actor with MessageListener with SvdUtils { 
// 
//  lazy val debug = props.bool("debug") getOrElse true
//  lazy val config = new ConnectionSvdConfiguration(props("xmppServer"), props.int("xmppPort") getOrElse 5222)
//  lazy val connection = new XMPPConnection(config)
//  lazy val presence = new Presence(Presence.Type.available)
//  lazy val login = props("xmppLogin") getOrElse "guest"
//  lazy val password = props("xmppPassword") getOrElse "nopassword"
//  lazy val resource = props("xmppResourceString") getOrElse "someResource"
//  lazy val gitRepositoryProjectDir = props("gitRepositoryProjectDir") getOrElse "/git/my_project.git"
// 
//  private var filter: AndFilter = null
//  private var chatmanager: ChatSvdManager = null
//  private var chat: List[Chat] = List()
//  
//  
//  def initConnection = {
//    XMPPConnection.DEBUG_ENABLED = false // NOTE: 2009-10-20 17:12:58 - dmilith - please look out for this bullshitting method added by some stupid fucks with requirement for X11 display
//    config.setCompressionEnabled(true)
//    config.setSASLAuthenticationEnabled(false)
//    connection.connect()
//    debug("*** l:"+login + " p:" + password + " r:" + resource)
//    try {
//      connection.login(login, password, resource)
//    } catch {
//      case x: Throwable => {
//        error("### Error while connecting to XMPP server. Please check login / password.")
//        debug( x.printStackTrace )
//                 exit
//      }
//    }
//    chatmanager = connection.getChatSvdManager
//    debug("*** num of users: " + chat.length)
//    propsh("users").foreach { x =>
//      try {
//        chat ::= chatmanager.createChat(x("user"), this)
//      } catch {
//        case x: Throwable => {
//          if (debug) {
//            info("### Error: " + x )
//          }
//        }
//      }
//    }
//    debug("*** num of users: " + chat.length)
//    presence.setStatus(props("xmppStatusDescription"))
//    presence.setMode(Presence.Mode.dnd)
//    connection.sendPacket(presence)
//    debug("*** Connected as: " + login + "\nReady to enter main loop")
//    SvdBot ! MainLoop
//  }
// 
//  def processMessage(chat: Chat, message: Message) {
//    debug("*** Received message: " + message + " (\"" + message.getBody + "\")")
// //   propsh("users").foreach { x =>
// //     x("user")
// //   }
//    if (message.getFrom.contains("dmilith") || message.getFrom.contains("vara")) {   // XXX: hardcoded value
//      info("Message contains dmilith: " + message.getFrom)
//      message.getBody match {
//        case "last" => {
//          chat.sendMessage("Requested last commit.\nNYI")
//        }
//        case "last5" => {
//          chat.sendMessage("Requested last 5 commits.\nNYI")
//        }
//        case "last10" => {
//          chat.sendMessage("Requested last 10 commits.\nNYI")
//        }
//        case "help" => {
//          chat.sendMessage("No help for noobs ;}")
//        }
//      }
//    }
//  }
//  
//  def closeConnection = {
//    connection.disconnect
//  }
//  
//  def getMessages: List[String] = {
//    var odb: ODB = null
//    var list: List[String] = List()
//    OdbSvdConfiguration.setAutomaticCloseFileOnExit(true)
//    OdbSvdConfiguration.setDatabaseCharacterEncoding( "UTF8" )
//    try {
//         odb = ODBFactory.openClient(props("xmppDatabaseListenAddress"), props("databaseODBPort"), props("xmppDatabaseName"))
//        var query = new CriteriaQuery(classOf[Commit], Where.equal("toRead", true))
//      query.orderByDesc("date") 
//      val commit = odb.getObjects(query)
//        while (commit.hasNext) {
//          val comm = (commit.next).asInstanceOf[Commit]
//            comm.toRead = false
//            odb.store(comm)
//            if (debug)
//              info("*** Found in database: " + comm.commitSha1)
//            list = list ::: List(comm.commitSha1)
//        }
//    } catch {
//      case x: Throwable => {
//        info("### Error in XMPPActor: " + x)
//        debug(x.printStackTrace)
//      }
//    } finally {
//      if (odb != null) { 
//        odb.close
//      } 
//    }
//    return list
//  }
//  
//  def tryToSendMessages = {
//    if (debug) print(".")
//    for ( commitSha <- getMessages ) {
//      chat.foreach { element =>
//        try {
//          if (commitSha.length > 0) {
//            if (debug) {
//              info("*** Trying to send messages, to User: " + element.getParticipant)
//            }
//            var currentUserPreferences: String = ""
//            propsh("users").foreach{
//              e => if (e("user") == element.getParticipant) currentUserPreferences = e("params")
//            }
//            val git = props("gitExecutable")
//            // NOTE: ListBuffer provides append method, and it should be used for large Lists
//            val showCommand = List(git, "--git-dir=" + gitRepositoryProjectDir, "show") ++ currentUserPreferences.split(' ') ++ List(commitSha)
//            val output = CommandExec.cmdExec(showCommand.toArray)
//            if (debug)
//              info("*** sent message length: " + output.length)
//            element.sendMessage(output)
//          }
//        } catch {
//          case e: Throwable => {
//            info("### Error " + e + "\nTrying to put commit onto list cause errors.")
//            DbAddCommit.writeCommitToDataBase(new Commit(commitSha))
//          }
//        }
//      }
//    }
//  }
//    
//  override def act = {
//    Actor.loop {
//      react {
//        case Init => {
//          initConnection // init connection after getting preferences
//          act
//        }
//        case Quit => {
//          debug("*** received Quit command, closing connection with XMPP server.")
//          closeConnection
//          exit
//        }
//        case ProcessMessages => {
//          tryToSendMessages
//          act
//        }
//        case _ => {
//          debug("*** received Unknown command.")
//          act
//        }
//      }
//    }
//  }
//  
// }