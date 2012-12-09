package com.verknowsys.served.notifications


import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.git._
import com.verknowsys.served.services._
import com.verknowsys.served.utils._

import org.jibble.pircbot._


class SvdIRCGate extends PircBot with Logging with SvdUtils with Gate {


    def settings = {
        setVerbose(false)
        setName("tasks-servant")
        setAutoNickChange(true)
        setVersion("0.1.0")
        setEncoding("UTF-8")
        connect("irc.freenode.net")
        joinChannel("#verknowsys")
    }


    def allowedUserNames = "dmilith" :: "tallica" :: "wick3d" :: Nil


    override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) {
        if (message.startsWith("!")) {
            message.split(" ").toList match {
                case "!ping" :: Nil =>
                    log.debug("Received ping request from: %s", sender)
                    sendMessage(channel, "%s: pong".format(sender))
                case "!tasks" :: nickname :: Nil =>
                    if (allowedUserNames.contains(nickname)) {
                        log.debug("Found allowed nickname: %s", nickname)
                        sendMessage(channel, "%s: No tasks sire.".format(nickname))
                    } else
                        log.trace("Not allowed nickname: %s sending command: '%s'", nickname, message)
            }
        }
    }


    def connect {
        log.info("Initiating SvdIRCGate")
        settings
    }


    def setStatus(st: String) {
    }


    def send(message: String) {

        // SvdNotifyMailer(message, SvdConfig.notificationMailRecipients)
    }


}

// // © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// // This Software is a close code project. You may not redistribute this code without permission of author.
//
// package com.verknowsys.served.gitbotnotifier
//
// import com.verknowsys.served.utils.props.Preferences
// import com.verknowsys.served.utils.signals.Quit
//
// import org.apache.log4j.Logger
// import scala.actors._
// import scala.actors.Actor._

// import org.neodatis.odb._
// import org.neodatis.odb.impl.core.query.criteria._
//
//
// // TODO: to be refactored - Preferences should be parametrized
// // 2009-10-29 12:54:11 - dmilith - NOTE: this module is deprecated, and temporarely wont be used and managed.
// object IRCActor extends PircBot with Actor {
//
//  private val logger = Logger.getLogger(IRCActor.getClass)
//  private val props = SvdBot.props
//
//  def settings = {
//    setVerbose(props("ircDebugInfo"))
//    setName(props("ircName"))
//    setAutoNickChange(props("ircAutoNickChange"))
//    setVersion(props("ircVersionString"))
//    setEncoding(props("ircEncoding"))
//    connect(props("ircServer"))
//    for (i <- props("ircAutoJoinChannels"))
//      joinChannel(i)
//  }
//
//  override def act = {
//    settings // XXX: IRC Actor is inactive ATM, it's useless now, maybe we could do something with it later. Maybe logging of irc chats?
//    react {
//      case Quit => {
//        disconnect
//      }
//    }
//  }
//
//  def getLinks(howMany: Int): List[SvdLinkInfo] = {
//    var odb: ODB = null
//    var list: List[SvdLinkInfo] = List()
//    OdbSvdConfiguration.setAutomaticCloseFileOnExit(true)
//    OdbSvdConfiguration.setDatabaseCharacterEncoding( "UTF8" )
//    try {
//        odb = ODBFactory.openClient(props("ircDatabaseListenAddress"), props("databaseODBPort"), props("ircDatabaseName"))
//        val query = new CriteriaQuery(classOf[SvdLinkInfo]) //, Where.equal("date.getDay", (new Date).getDay))
//      val link = odb.getObjects(query.orderByDesc("date"))
//        while (link.hasNext && (list.size <= howMany)) {
//          val comm = (link.next).asInstanceOf[SvdLinkInfo]
//          list ::= comm
//        }
//    } catch {
//      case x: Throwable => {
//        info("### Error in getLinks: " + x)
//        info(x.printStackTrace)
//      }
//    } finally {
//      if (odb != null) {
//        odb.close
//      }
//    }
//    return list
//  }
//
//  def putLinkToDatabase(arg: SvdLinkInfo) {
//    var odb: ODB = null
//    try {
//      odb = ODBFactory.openClient(props("ircDatabaseListenAddress"), props("databaseODBPort"), props("ircDatabaseName"))
//      odb.store( arg )
//      odb.commit
//    } catch {
//      case x: Throwable => {
//        info("### Error: There were problems while connecting to ODB server.")
//      }
//    } finally {
//      if (odb != null) {
//        odb.close
//      }
//    }
//  }
//
//  override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) {
//    if (message.contains("http://") || message.contains("www.")) {
//      val link = new SvdLinkInfo(sender, channel, "\"" + message + "\"")
//      putLinkToDatabase(link)
//    }
//    try {
//      if (message.split(' ')(0).equalsIgnoreCase("!links") && message.split(' ')(1).length > 2) {
//        sendMessage( sender, "You requested, to find links which contain: \"" + message.split(' ')(1) + "\"…" )
//        var msg = ""
//        for (link <- getLinks(10000)) { // XXX hardcoded max of 10.000 links to search in
//          if (link.message.toUpperCase.contains(message.split(' ')(1).toUpperCase)) {
//            msg = "On: " + link.channel + " @(" + link.date.toString + "), by " + link.author + ": " + link.message
//            sendMessage( sender, msg )
//            Thread.sleep(1000)
//          }
//        }
//      }
//    } catch {
//      case x: Throwable => {
//        sendMessage( sender, "Taking 10 last links with their context:" )
//        var msg = ""
//        for (link <- getLinks(10)) {
//          msg = "On: " + link.channel + " @(" + link.date.toString + ", by " + link.author + ": " + link.message
//          sendMessage( sender, msg )
//          Thread.sleep(1000)
//        }
//      }
//    }
//  }
//
//  override def onDisconnect = {
//    try {
//      settings
//    } catch {
//      case x: Throwable => {
//        info("### Disconnected and cannot connect again! I'll keep trying.. " + x.getMessage)
//        settings
//      }
//    }
//  }
//
// }