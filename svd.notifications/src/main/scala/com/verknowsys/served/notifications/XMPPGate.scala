package com.verknowsys.served.notifications

import scala.actors._
import scala.collection.mutable.ListBuffer

import com.verknowsys.served.utils.Logged
    
import org.jivesoftware.smack._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.filter._

class XMPPGate(host: String, port: Int, login: String, password: String, resource: String) extends Gate with MessageListener with Logged {
    val config = new ConnectionConfiguration(host, port)
    val connection = new XMPPConnection(config)
    val presence = new Presence(Presence.Type.available)
    val chats = ListBuffer[Chat]()
    
    def connect {
        logger.debug("Initiating XMPPGate connection")
        // XMPPConnection.DEBUG_ENABLED = true
        config.setCompressionEnabled(true)
        config.setSASLAuthenticationEnabled(true)
        connection.connect
        
        try {
            connection.login(login, password, resource)
            logger.debug("XMPP: login: " + login + ", pass:" + password + ", resource:" + resource)
        } catch {
            case x: Throwable =>
                logger.error("Error while connecting to XMPP server. Please check login / password.")
                logger.debug( x.printStackTrace )
        }
        
        val chatmanager = connection.getChatManager

        jids.foreach { user =>
            try {
                chats += chatmanager.createChat(user, this)
            } catch {
                case x: Throwable => logger.info("Error: " + x )
            }
        }
        
        logger.trace("Number of users bound to be notified with repository changes: %s".format(chats.length))
        presence.setStatus("ServeD Git Bot Notifier | NC")
        presence.setMode(Presence.Mode.dnd)
        connection.sendPacket(presence)
    }
    
    def disconnect { connection.disconnect }
    
    def setStatus(st: String) {
        presence.setStatus(st)
    }
    
    def send(message: String) {
        chats.foreach { chatRecipient =>
            try {
                logger.debug("Trying to send messages, to User: " + chatRecipient.getParticipant)
                chatRecipient.sendMessage(message)
                logger.trace("Sent message: " + message + " length: " + message.length)
            } catch {
                case e: Throwable =>
                    logger.info("### Error " + e + "\nTrying to put commit onto list cause errors.")
            }
        }
    }
    
    def processMessage(chat: Chat, message: Message) {
        logger.trace("Received message: " + message + " (\"" + message.getBody + "\")")
        // if (message.getFrom.contains("verknowsys.com")) {   // XXX: hardcoded value
        //     logger.trace("Message contains verknowsys: " + message.getFrom)
        //     message.getBody match {
        //         case "last" =>
        //             chat.sendMessage("Requested last commit.\nNYI")
        //         case "last5" =>
        //             chat.sendMessage("Requested last 5 commits.\nNYI")
        //         case "last10" =>
        //             chat.sendMessage("Requested last 10 commits.\nNYI")
        //         case "help" =>
        //             chat.sendMessage("No help for noobs")
        //     }
        // }
    }
    

    
    def jids = "dmilith@verknowsys.com" :: "i@teamon.eu" :: Nil // XXX: Hardcoded
}