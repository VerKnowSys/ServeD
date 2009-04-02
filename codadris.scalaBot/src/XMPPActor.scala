// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import scala.actors._
import scala.collection.immutable.HashMap

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria._
import org.neodatis.odb.core.query.criteria._

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
	private val resource = "scalaBot_0.3"
	
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
			HashMap( "user" -> "dmilith@drakor.eu", "settings" -> "--numstat --no-merges" ),
			HashMap( "user" -> "szymon@jez.net.pl", "settings" -> "--full-diff --numstat --no-merges" )
		)
	}
	
	def closeConnection = {
		connection.disconnect
	}
	
	def getMessages: List[String] = {
		var odb: ODB = null
		var list: List[String] = List()
		try {
		    odb = ODBFactory.open(ScalaBot.databaseName)
		    val query = new CriteriaQuery(classOf[Commit], Where.equal("toRead", true))
			val commit = odb.getObjects(query)
				while (commit.hasNext) {
					commit.next match {
						case comm: Commit => {
							comm.toRead = false
							odb.store(comm)
							odb.commit
							if (debug)
								println("*** Found in database: " + comm.commitSha1)
							list = list ::: List(comm.commitSha1)
						}
					}
				}
			odb.close
		} catch {
			case x: Throwable => {
				println("### Error: " + x)
			}
		}
		return list
	}
	
	def tryToSendMessages = {
		if (debug) print(".")
			chat.foreach { element =>
				try {
					for ( commitSha <- getMessages ) {
						if (commitSha.length > 0) {
							if (debug) {
								println("*** Trying to send messages, to User: " + element.getParticipant)
							}
							var currentUserSettings: String = ""
							getUsers.foreach{ 
								e => if (e("user") == element.getParticipant) currentUserSettings = e("settings")
							}
							val a = currentUserSettings.split(' ')
							// XXX: only 5 arguments max:
							val showCommand = Array("git","show", a(0), a(1), a(2), a(3), a(4), commitSha) 
							val output = CommandExec.cmdExec(showCommand)
							println("*** " + output)
							element.sendMessage(output)
						}
					}
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
						case 'ProcessMessages => {
							initConnection
							tryToSendMessages
							act
						}
						case _ => {
							if (debug) println("received Unknown command.")
							act
						}
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