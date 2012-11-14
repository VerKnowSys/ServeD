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


class SvdXMPPGate(host: String, port: Int, login: String, password: String, resource: String, account: SvdAccount, accountManager: ActorRef) extends Gate with MessageListener with Logging with SvdUtils {

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
        presence.setMode(Presence.Mode.available)
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
                "whoami" -> "Shows current user name"
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
                        accountManager ! System.RegisterDomain(domain)

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


            // USER SIDE REMOTE API
            //
            //
            // Services API
            //
            // service status all
            // service status Redis
            // service stop Redis
            // service stop all
            // service start Redis
            // service start all
            //
            // Logs API
            // logs show all
            // logs show Redis
            //
            // Register/ UnRegister API
            // register domain domain.some.com
            // register user user name
            //
            // Auth API
            // TODO: auth API
            //
            //

            case anything =>
                send(formatMessage("E:Try 'help'. No such command: %s".format(anything.mkString(" "))))

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