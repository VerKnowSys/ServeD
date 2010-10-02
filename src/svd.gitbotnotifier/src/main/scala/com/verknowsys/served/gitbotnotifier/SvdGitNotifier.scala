// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.gitbotnotifier

import com.verknowsys.served.git._
import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._

// import com.verknowsys.served.utils.signals.{ProcessMessages, MainLoop, Init, Quit}
// import java.io.OutputStreamWriter
// import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
import scala.actors._
import scala.io._
import org.jivesoftware.smack._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.filter._
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._


class SvdGitNotifier(repo: GitRepository) extends Actor with MessageListener with Utils {


    def processMessage(chat: Chat, message: Message) {
        logger.debug("Received message: " + message)
        logger.debug("*** Received message: " + message + " (\"" + message.getBody + "\")")
        if (message.getFrom.contains("verknowsys.com")) {   // XXX: hardcoded value
            logger.debug("Message contains verknowsys: " + message.getFrom)
            message.getBody match {
                case "last" =>
                    chat.sendMessage("Requested last commit.\nNYI")
                case "last5" =>
                    chat.sendMessage("Requested last 5 commits.\nNYI")
                case "last10" =>
                    chat.sendMessage("Requested last 10 commits.\nNYI")
                case "help" =>
                    chat.sendMessage("No help for noobs")
            }
        }
    }
        
    
    def act {

        val config = new ConnectionConfiguration("verknowsys.com", 65222) // XXX: hardcode
        val connection = new XMPPConnection(config)
        val presence = new Presence(Presence.Type.available)
        val login = "git-bot" // XXX: hardcode
        val password = "git-bot-666" // XXX: hardcode
        val resource = "served-bot-resource" // XXX: hardcode
        val chat = ListBuffer[Chat]()
        
        
        def initConnection = {
            // XMPPConnection.DEBUG_ENABLED = true
            config.setCompressionEnabled(true)
            config.setSASLAuthenticationEnabled(true)
            connection.connect
            logger.debug("XMPP: login: " + login + ", pass:" + password + ", resource:" + resource)
            try {
                connection.login(login, password, resource)
            } catch {
                case x: Throwable =>
                    logger.error("Error while connecting to XMPP server. Please check login / password.")
                    logger.debug( x.printStackTrace )
                    exit
            }
            val chatmanager = connection.getChatManager
            
            ("dmilith@verknowsys.com" :: "i@teamon.eu" :: Nil).foreach { user =>
                try {
                    chat += chatmanager.createChat(user, this)
                } catch {
                    case x: Throwable =>
                        logger.info("Error: " + x )
                }
            }
            logger.debug("Number of users: " + chat.length)
            presence.setStatus("ServeD Git Bot Notifier")
            presence.setMode(Presence.Mode.dnd)
            connection.sendPacket(presence)
        }
        

        def closeConnection = connection.disconnect

        
        val watchHEAD = FileEvents.watch(repo.dir + "/.git/logs") { name => name match {
            case "HEAD" =>
                val headSha = Source.fromFile(repo.dir + "/.git/refs/heads/master").mkString.trim // XXX: hardcoded
                logger.debug("HEAD: %s changed in repo: %s".format(headSha, repo.dir))
                val commit = repo.history("f182450863da40028f75a2166d1b9c9934b1c7cc", headSha).map{ e => e.message }.mkString(", ") // XXX: hardcoded
                logger.debug("Commit: " + commit)
                chat.foreach { chatRecipient =>
                    try {
                        if (commit != null) {
                            logger.debug("Trying to send messages, to User: " + chatRecipient.getParticipant)
                            chatRecipient.sendMessage(commit)
                            logger.debug("Sent message: " + commit + " length: " + commit.length)
                        } else {
                            chatRecipient.sendMessage("Emptiness")
                        }
                    } catch {
                        case e: Throwable =>
                            logger.info("### Error " + e + "\nTrying to put commit onto list cause errors.")
                            // DbAddCommit.writeCommitToDataBase(new Commit(commitSha))
                    }
                }
                
            case x: AnyRef =>
                logger.warn("Command not recognized. GitNotifier will ignore You: " + x.toString)
        }}
        
        
        Actor.loop {
            receive {
                case Init =>
                    initConnection
                    logger.info("Git Notifier ready")
                    
                case Quit =>
                    logger.info("Quitting Git Notifier")
                    closeConnection
                    exit
                    
                case x: AnyRef =>
                    logger.warn("Command not recognized. GitNotifier will ignore signal: " + x.toString)
                
            }
        }
    }
    
}


 // addShutdownHook {
 //   XMPPActor ! Quit
 //    ODBServerActor ! Quit
 //  IRCActor ! Quit
 //   SvdBot ! Quit
 //   logger.info("Done\n")
 // }
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