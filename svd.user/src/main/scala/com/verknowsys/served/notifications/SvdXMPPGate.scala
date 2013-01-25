/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.notifications


import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._

import scala.collection.mutable.ListBuffer

import akka.actor._
import org.jivesoftware.smack._
import org.jivesoftware.smack._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.filter._


class SvdXMPPGate(host: String, port: Int, login: String, password: String, resource: String, account: SvdAccount, accountManager: ActorRef) extends SvdActor with MessageListener {


    val config = new ConnectionConfiguration(host, port)
    config.setCompressionEnabled(SvdConfig.notificationXmppCompression)
    config.setSASLAuthenticationEnabled(SvdConfig.notificationXmppUseSasl)
    val connection = new XMPPConnection(config)
    val presence = new Presence(Presence.Type.available)
    val chats = ListBuffer[Chat]()


    def receive = {


        case Notify.Connect =>
            log.info("Connecting XMPP Gate to host: %s", host)
            try {
                connection.connect
                log.trace("SvdXMPPGate connected to server")
                connection.login(login, password, resource)
                log.trace("XMPP server: %s:%d, login: %s, resource: %s", host, port, login, resource)
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
                presence.setStatus("ServeD® XMPP Notification Gate")
                presence.setMode(Presence.Mode.available)
                connection.sendPacket(presence)

            } catch {
                case x: Throwable =>
                    log.error("Error in XMPP Gate Exc: %s".format(x))
                    log.warn("XMPP Gate initialization aborted for this session.")
            }


        case Notify.Disconnect =>
            log.info("Disconnecting from XMPP server: %s", host)
            connection.disconnect()


        case Notify.Status(status) =>
            log.debug("Setting status: %s for XMPP Gate.", status)
            presence.setStatus(status)


        case Notify.Message(message) =>
            log.debug("Sending message: %s", message)
            send(message)


    }


    override def preStart {
        super.preStart
        log.info("Launching XMPP Notifications Gate")
    }


    override def postStop {
        log.info("Terminating XMPP Notifications Gate")
        super.postStop
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
        val from = message.getFrom
        val msgList = msg.toLowerCase.split(" ").toList
        log.debug("Received command message: (%s)".format(msg))

        val commands = Map(
            // "help" -> "This console provides remote control over your account",
            "services" -> Map(
                "remove" -> "Remove services",
                "start" -> "Start services (will also install services on demand if not already built)",
                "stop" -> "Stop services",
                "status" -> "Show services details",
                "store" -> "Store service state",
                "stored" -> "Show stored services"
                ),
            "service" -> Map(
                "remove" -> "Removes service",
                "start" -> "Starts service (will also install service on demand if not already built)",
                "stop" -> "Stops service",
                "status" -> "Shows service details"
                ),
            "show" -> Map(
                "logs" -> "Shows logs from service given as a param",
                "whoami" -> "Shows current user name",
                "scheduler" -> "Shows info about scheduled tasks"
                ),
            "register" -> Map(
                "domain" -> "Registers domain"
                ),
            "unregister" -> Map(
                "domain" -> "Unregisters domain"
                // "user" -> "Unregisters domain"
                )
            )

        send(formatMessage("I:> '%s' from %s".format(msg, from)))
        msgList match {

            case "help" :: Nil =>
                send("help -> %s".format(commands.keys.mkString(", ")))

            case "help" :: patt :: Nil =>
                patt.toLowerCase match {
                    case key: String =>
                        send("%s -> %s".format(patt, commands(key).keys.mkString(", ")))
                }

            case "help" :: patt :: inner :: Nil =>
                patt.toLowerCase match {
                    case key: String =>
                        send(commands(key)(inner))
                }

            case "show" :: command :: Nil =>
                command.toLowerCase match {
                    case "whoami" =>
                        send("You're authorized as %s. Writing from: %s".format(account.userName, from))
                    case "scheduler" =>
                        send("%s".format("dsd"))
                }

            case "show" :: command :: argument :: Nil =>
                command.toLowerCase match {
                    case "log" =>
                        accountManager ! User.ReadLogFile(argument, None)
                }

            case "show" :: command :: argument :: pattern :: Nil =>
                command.toLowerCase match {
                    case "log" =>
                        accountManager ! User.ReadLogFile(argument, Some(pattern))
                }

            case "register" :: command :: argument :: _ =>
                val domain = argument.toLowerCase
                command.toLowerCase match {
                    case "domain" =>
                        accountManager ! System.RegisterDomain(domain, accountManager)

                    // case "user" =>
                    //     accountManager ! User.TerminateServices
                }

            case "services" :: command :: Nil =>
                command.toLowerCase match {
                    case "store" | "save" =>
                        accountManager ! User.StoreServices

                    case "stored" | "saved" =>
                        accountManager ! User.GetStoredServices

                    case "start" =>
                        accountManager ! User.SpawnServices

                    case "stop" =>
                        accountManager ! User.TerminateServices

                    case "status" | "list" | "all" | "show" | "running" | "run" | "spawned" =>
                        accountManager ! User.GetRunningServices

                    case "avail" | "available" =>
                        accountManager ! User.ShowAvailableServices

                }

            case "service" :: argument :: command :: Nil =>
                val serviceName = argument.capitalize
                serviceName match {
                    case _ =>
                        command.toLowerCase match {
                            case "start" =>
                                accountManager ! User.SpawnService(serviceName)

                            case "stop" =>
                                accountManager ! User.TerminateService(serviceName)

                            case "status" | "list" | "all" | "show" =>
                                accountManager ! User.GetServiceStatus(serviceName)
                        }
                }


            case anything =>
                send(formatMessage("E:Try 'help'. No such command: %s".format(anything.mkString(" "))))

        }

    }

}
