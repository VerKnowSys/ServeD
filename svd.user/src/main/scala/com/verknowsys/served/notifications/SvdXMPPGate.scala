package com.verknowsys.served.notifications


import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.git._
import com.verknowsys.served.services._
import com.verknowsys.served.utils._

import scala.collection.mutable.ListBuffer
import org.jivesoftware.smack._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.filter._

import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import akka.actor._


class SvdXMPPGate(host: String, port: Int, login: String, password: String, resource: String, accountManager: ActorRef) extends Gate with MessageListener with Logging with SvdUtils {

    val config = new ConnectionConfiguration(host, port)
    config.setCompressionEnabled(SvdConfig.notificationXmppCompression)
    config.setSASLAuthenticationEnabled(SvdConfig.notificationXmppUseSasl)
    val connection = new XMPPConnection(config)
    val presence = new Presence(Presence.Type.available)
    val chats = ListBuffer[Chat]()


    def connect {
        log.info("Initiating SvdXMPPGate")
        // XMPPConnection.DEBUG_ENABLED = SvdConfig.notificationXmppDebug

        connection.connect
        log.trace("SvdXMPPGate connected to server")

        try {
            connection.login(login, password, resource)
            log.trace("XMPP server: %s:%d, login: %s, resource: %s", host, port, login, resource)
        } catch {
            case x: Throwable =>
                log.error("Error while connecting to Xmpp server. Please check login / password. Exc: %s".format(x))
                // log.debug( x.printStackTrace.toString )
        }

        val chatmanager = connection.getChatManager

        SvdConfig.notificationXmppRecipients.foreach { user =>
            try {
                chats += chatmanager.createChat(user, this)
            } catch {
                case x: Throwable =>
                    log.error("Error: " + x )
            }
        }

        log.debug("Number of users bound to be notified with repository changes: %s".format(chats.length))
        presence.setStatus("ServeD® XMPP Notification Plugin")
        presence.setMode(Presence.Mode.dnd)
        connection.sendPacket(presence)
    }

    def disconnect { connection.disconnect() }

    def setStatus(st: String) {
        presence.setStatus(st)
    }

    def send(message: String) {
        chats.foreach { chatRecipient =>
            try {
                log.trace("Trying to send messages, to User: " + chatRecipient.getParticipant)
                chatRecipient.sendMessage(message)
                log.trace("Sent message: " + message + " length: " + message.length)
            } catch {
                case e: Throwable =>
                    log.error("Error sending message caused: %s", e)
            }
        }
    }


    def processMessage(chat: Chat, message: org.jivesoftware.smack.packet.Message) {
        val msg = message.getBody
        log.debug("Received command message: (%s)".format(msg))

        val commands = Map(
            "help" -> "This console provides remote control over your account",
            "log" -> "Shows log of service given as param",
            "install" -> "Installs software given as param",
            "show" -> "Shows installed software",
            "register" -> "Registering"
            )

        msg.split(" ").toList match {

            case "help" :: Nil =>
                send("Available commands: %s".format(commands.mkString(", ")))

            case "help" :: command :: Nil =>
                send("Detailed information about command: %s\n%s".format(command, commands(command)))

            case "log" :: app :: Nil =>
                send("Last 10 lines of log of app: %s".format(app))
                accountManager ! "Dupa"

            case "install" :: app :: Nil =>
                send("Installing %s".format(app))

            case "register" :: "domain" :: domain :: _ :: Nil =>
                send("Registering domain: %s".format(domain))
                accountManager ! System.RegisterDomain(domain)

            case "show" :: Nil =>
                (accountManager ? User.GetServices) onSuccess {
                    case x =>
                        send("Showing installed software:\n%s".format(x))

                }

            case "terminate" :: "all" :: Nil =>
                send("Terminating all running services")
                (accountManager ? User.TerminateServices) onSuccess {
                    case x =>
                        send("Terminated")
                }

            case "launch" :: "all" :: Nil =>
                send("Launching all installed services")
                (accountManager ? User.SpawnServices) onSuccess {
                    case x =>
                        send("Terminated")
                }


            case anything =>
                send("Bad command: %s".format(anything.mkString(" ")))

        }

        // send(message.getBody)
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


}