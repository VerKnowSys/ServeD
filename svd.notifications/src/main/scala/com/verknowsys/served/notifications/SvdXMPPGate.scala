package com.verknowsys.served.notifications


import com.verknowsys.served._
import com.verknowsys.served.utils.Logging

import scala.collection.mutable.ListBuffer
import org.jivesoftware.smack._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.filter._


class SvdXMPPGate(host: String, port: Int, login: String, password: String, resource: String) extends Gate with MessageListener with Logging {
    
    val config = new ConnectionConfiguration(host, port)
    val connection = new XMPPConnection(config)
    val presence = new Presence(Presence.Type.available)
    val chats = ListBuffer[Chat]()
    

    def connect {
        log.debug("Initiating SvdXmppGate connection")
        XMPPConnection.DEBUG_ENABLED = SvdConfig.notificationXmppDebug
        config.setCompressionEnabled(SvdConfig.notificationXmppCompression)
        config.setSASLAuthenticationEnabled(SvdConfig.notificationXmppUseSasl)
        connection.connect
        
        try {
            connection.login(login, password, resource)
            log.debug("Xmpp login: " + login + ", pass:" + password + ", resource:" + resource)
        } catch {
            case x: Throwable =>
                log.error("Error while connecting to Xmpp server. Please check login / password.")
                log.debug( x.printStackTrace.toString )
        }
        
        val chatmanager = connection.getChatManager

        jids.foreach { user =>
            try {
                chats += chatmanager.createChat(user, this)
            } catch {
                case x: Throwable =>
                    log.error("Error: " + x )
            }
        }
        
        log.trace("Number of users bound to be notified with repository changes: %s".format(chats.length))
        presence.setStatus("ServeD® Xmpp Notifier")
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
                log.debug("Trying to send messages, to User: " + chatRecipient.getParticipant)
                chatRecipient.sendMessage(message)
                log.trace("Sent message: " + message + " length: " + message.length)
            } catch {
                case e: Throwable =>
                    log.info("### Error " + e + "\nTrying to put commit onto list cause errors.")
            }
        }
    }
    
    def processMessage(chat: Chat, message: org.jivesoftware.smack.packet.Message) {
        log.trace("Received message: " + message + " (\"" + message.getBody + "\")")
        // if (message.getFrom.contains("verknowsys.com")) {
        //     trace("Message contains verknowsys: " + message.getFrom)
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
    

    
    def jids = SvdConfig.notificationXmppRecipients.split(",")
    
    
}