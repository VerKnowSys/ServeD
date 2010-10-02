// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.gitbotnotifier

import com.verknowsys.served.git._
import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import scala.actors._
import scala.io._
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat
import org.jivesoftware.smack._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.filter._

/**
 *   Git repository XMPP notifier
 *
 *   @author dmilith, teamon
 *
 *   @example
 *       import com.verknowsys.served.gitbotnotifier._
 *       import com.verknowsys.served.git._
 *       import com.verknowsys.serverd.utils.signals._
 *
 *       val gitNotifier = new SvdGitNotifier("/path/to/repository"))
 *       gitNotifier.start
 *       gitNotifier ! Init
 *
 */
class SvdGitNotifier(repo: GitRepository) extends Actor with MessageListener with Utils {

    /**
     *   Handles incomming messages
     *   @author dmilith
     */
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

    val config = new ConnectionConfiguration("verknowsys.com", 65222) // XXX: hardcode
    val connection = new XMPPConnection(config)
    val presence = new Presence(Presence.Type.available)
    val login = "git-bot" // XXX: hardcode
    val password = "git-bot-666" // XXX: hardcode
    val resource = "served-bot-resource" // XXX: hardcode
    val chat = ListBuffer[Chat]()
    var oldHEAD = repo.head // XXX: var :(


    def act {

        def initConnection = {
            // XMPPConnection.DEBUG_ENABLED = true
            config.setCompressionEnabled(true)
            config.setSASLAuthenticationEnabled(true)
            connection.connect
            try {
                connection.login(login, password, resource)
                logger.trace("XMPP: login: " + login + ", pass:" + password + ", resource:" + resource)
            } catch {
                case x: Throwable =>
                    logger.error("Error while connecting to XMPP server. Please check login / password.")
                    logger.debug( x.printStackTrace )
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
            logger.trace("Number of users: " + chat.length)
            presence.setStatus("ServeD Git Bot Notifier")
            presence.setMode(Presence.Mode.dnd)
            connection.sendPacket(presence)
        }


        def closeConnection = connection.disconnect


        // XXX: This doesnt work for bare repos.
        // in bare there is refs/heads/master file instead of .git/logs/HEAD
        // change this to FileEvents.watchFile(repo.headPath) { ... }
        val watchHEAD = FileEvents.watch(repo.dir + "/.git/logs") { name => name match {
            case "HEAD" =>
                logger.debug("HEAD changed in repo: %s".format(repo.dir))

                repo.history(oldHEAD).foreach { commit =>
                    logger.trace("Commit: " + commit)
                    val message = "%s\n%s %s\n%s".format(commit.sha, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(commit.date), commit.author.nameAndEmail, commit.message)

                    chat.foreach { chatRecipient =>
                        try {
                            logger.debug("Trying to send messages, to User: " + chatRecipient.getParticipant)
                            chatRecipient.sendMessage(message)
                            logger.trace("Sent message: " + message + " length: " + message.length)
                        } catch {
                            case e: Throwable =>
                                logger.info("### Error " + e + "\nTrying to put commit onto list cause errors.")
                                // DbAddCommit.writeCommitToDataBase(new Commit(commitSha))
                        }
                    }
                }

                oldHEAD = repo.head


            case x: AnyRef =>
                logger.warn("Command not recognized. GitNotifier will ignore You: " + x.toString)
        }}


        Actor.loop {
            receive {
                case Init =>
                    initConnection
                    logger.info("Git Notifier ready")

                case Quit =>
                    closeConnection
                    logger.info("Quitting Git Notifier")
                    // exit

                case x: AnyRef =>
                    logger.warn("Command not recognized. GitNotifier will ignore signal: " + x.toString)

            }
        }
    }

}

// XXX: Remove this
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