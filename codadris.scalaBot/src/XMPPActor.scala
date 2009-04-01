// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved. 

package scalabot

import scala.actors._
import scala.collection.immutable.HashMap

import org.jivesoftware.smack._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.filter._
import org.jivesoftware.smack.PacketCollector
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smackx._

object XMPPActor extends Actor with MessageListener { // with PacketListener 
	
	val debug = ScalaBot.debug
	
	private val config = new ConnectionConfiguration("drakor.eu", 5222)
	private val connection = new XMPPConnection(config)	
	private val presence = new Presence(Presence.Type.unavailable)
	private val login = "git-bot"
	private val password = "git-bot-666"
	private val resource = "scalaBot_0.2"
	
	var filter: AndFilter = null
	var chatmanager: ChatManager = null
	var chat: List[Chat] = List()
	
	def initConnection = {
		XMPPConnection.DEBUG_ENABLED = true
		config.setCompressionEnabled(true)
		config.setSASLAuthenticationEnabled(false)
		connection.connect
		connection.login(login, password, resource)
		chatmanager = connection.getChatManager
		println("num: " + chat.length)
		for ( x <- getUsers) {
			try {
				chat = chat ::: List( chatmanager.createChat(x("user"), this) )
			} catch {
				case x: Throwable => {
					if (debug) {
						println("### Error: " + x )
					}
				}
			}
		}
		println("num: " + chat.length)
		
// 		chat = chatmanager.createChat("dmilith@drakor.eu", this)
		// assert user("user") != Nothing
		// if (debug) println("myMap(\"user\": " + user("user"))	
		
		presence.setStatus("I'm still in development so stay away")
		connection.sendPacket(presence)
		//filter = new AndFilter( new PacketTypeFilter( classOf[Message] ), new FromContainsFilter("dmilith@drakor.eu") )
		//connection.addPacketListener(this, filter)
		println("Connected as: " + login)
	}

	// def processPacket(packet: Packet) {
	// 	val message = packet.asInstanceOf[Message]
	// 		try {
	// 			chat.sendMessage(message.getBody());
	// 		} catch {
	// 			case x: XMPPException => {
	// 				println("XMPP exception: " + x )
	// 			}
	// 		}
	// 	//connection.sendPacket(packet)
	// 	//sendMessage("dmilith@drakor.eu",packet.asInstanceOf[Message].getBody)
	// 	println("processPacket: " + packet)
	// 	Console.flush
	// }

	def processMessage(chat: Chat, message: Message) {
		if (debug) {
			println("*** Received message: " + message + " (\"" + message.getBody + "\")")
		}
	}
	
	def getUsers = {
		List(
			HashMap( "user" -> "dmilith@drakor.eu", "settings" -> "-p -v" ),
			HashMap( "user" -> "szymon@jez.net.pl", "settings" -> "-p" )
		)
	}
	
	def closeConnection = {
		connection.disconnect
	}
	
	def sendMessages( msg: String ) = {
		chat.foreach { element =>
			if (debug) {
				try {
					if (debug) {
						println("*** Sending message: " + msg + ", to User: " + element.getParticipant)
					}
					element.sendMessage(msg + " -> " + element.getParticipant)
				} catch {
					case e: XMPPException => {
						if (debug) {
							println("Error Delivering block")
						}
						throw new Exception("### Error in sendMessage")
					}
				}
			}
		}
		//val chatmanager = connection.getChatManager
		//val newChat = chatmanager.createChat( targetXMPP, this )
	}
			
		
	override def act = {
		Actor.loop {
			react {
				case y: Symbol =>
					y match {
						case 'Quit => {
							if (debug) println("received Quit command.")
							exit
						}
						case 'InitConnection => {
							if (debug) println("received InitConnection command.")
							initConnection
							act
						}
						case 'CloseConnection => {
							if (debug) println("received CloseConnection command.")
							closeConnection
						}
						case _ => {
							if (debug) println("received Unknown command.")
							act
						}
					}
				case z: Commit => {
					sendMessages(z.commitMessage + " @" + z.branch)
					act
				}
				case _ => {
					if (debug) 
						println(" ")
					act
				}
			}
		}
	}
	
}