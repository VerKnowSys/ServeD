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
	
	val debug = true
	
	private val config = new ConnectionConfiguration("drakor.eu", 5222)
	private val connection = new XMPPConnection(config)	
	private val presence = new Presence(Presence.Type.unavailable)
	private val login = "git-bot"
	private val password = "git-bot-666"
	private val resource = "scalaBot_0.4"
	val repository_dir = "/git/codadris.git/.git"
	
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
		getUsers.foreach { x =>
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
		presence.setStatus("I'm quite ready to serve!")
		connection.sendPacket(presence)
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
			// XXX: only three arguments in settings:
			HashMap( "user" -> "dmilith@drakor.eu", "settings" -> "--numstat --no-merges --no-merges" ),
			HashMap( "user" -> "szymon@jez.net.pl", "settings" -> "--full-diff --numstat --no-merges" ),
			HashMap( "user" -> "karolrvn@jabber.verknowsys.info", "settings" -> "--numstat --no-merges --no-merges" )
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
		for ( commitSha <- getMessages ) {
			chat.foreach { element =>
				try {
					if (commitSha.length > 0) {
						if (debug) {
							println("*** Trying to send messages, to User: " + element.getParticipant)
						}
						var currentUserSettings: String = ""
						getUsers.foreach{ 
							e => if (e("user") == element.getParticipant) currentUserSettings = e("settings")
						}
						val a = currentUserSettings.split(' ')
						// XXX: only 2 arguments max:
						val showCommand = Array("git",  "--git-dir="+ repository_dir +"","show", a(0), a(1), a(2), commitSha)
						val output = CommandExec.cmdExec(showCommand)
						if (debug)
							println("*** sent message length: " + output.length)
						element.sendMessage(output)
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