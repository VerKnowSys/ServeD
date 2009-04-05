// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import commiter._

import scala.actors._

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria._
import org.neodatis.odb.core.query.criteria._
// import org.neodatis.odb.Configuration

import org.jivesoftware.smack._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.filter._
import org.jivesoftware.smack.PacketCollector
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smackx._



object XMPPActor extends Actor with MessageListener { // with PacketListener 
	
	private val prefs = (new Preferences).loadPreferences
	private val debug = prefs.getb("debug")
	private val config = new ConnectionConfiguration(prefs.get("server"), prefs.geti("port"))
	private val connection = new XMPPConnection(config)	
	private val presence = new Presence(Presence.Type.unavailable)
	private val login = prefs.get("login")
	private val password = prefs.get("password")
	private val resource = prefs.get("resource")
	private val repositoryDir = prefs.get("repositoryDir")

	private var filter: AndFilter = null
	private var chatmanager: ChatManager = null
	private var chat: List[Chat] = List()
	
	def initConnection = {
		// Configuration.useMultiThread(true, 5)
		// Configuration.setDatabaseCharacterEncoding("UTF-8")
		XMPPConnection.DEBUG_ENABLED = true
		config.setCompressionEnabled(true)
		config.setSASLAuthenticationEnabled(false)
		connection.connect
		if (debug) println("*** l:"+login + " p:" + password + " r:" + resource)
		try {
			connection.login(login, password, resource)
		} catch {
			case x: Throwable => {
				println("### Error while connecting to XMPP server. Please check login / password.")
				if (debug) println( x.printStackTrace )
			}
		}
		chatmanager = connection.getChatManager
		if (debug) println("*** num of users: " + chat.length)
		prefs.getl("users").foreach { x =>
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
		if (debug) println("*** num of users: " + chat.length)
		presence.setStatus(prefs.get("statusDescription"))
		connection.sendPacket(presence)
		if (debug) println("*** Connected as: " + login)
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
		if (debug) println("*** Received message: " + message + " (\"" + message.getBody + "\")")
	}
	
	def closeConnection = {
		connection.disconnect
	}
	
	def getMessages: List[String] = {
		var odb: ODB = null
		var list: List[String] = List()
		try {
		    odb = ODBFactory.openClient("127.0.0.1",50603,"commitDatabase")
			// try { //adding indexes before queries
			// 				odb.getClassRepresentation(classOf[Commit]).addUniqueIndexOn("commitSha1", Array("commitSha1"), true)
			// 				odb.getClassRepresentation(classOf[Commit]).addUniqueIndexOn("toRead", Array("toRead"), true)
			// 				if (debug) println("*** Indexes were added")
			// 			} catch {
			// 				case y: Throwable => {
			// 					// XXX: NOOP
			// 				}
			// 			}
		    var query = new CriteriaQuery(classOf[Commit], Where.equal("toRead", true))
			query.orderByDesc("date") 
			val commit = odb.getObjects(query)
				while (commit.hasNext) {
					val comm = (commit.next).asInstanceOf[Commit] //match {
						//case comm: Commit => {
						comm.toRead = false
						odb.store(comm)
						if (debug)
							println("*** Found in database: " + comm.commitSha1)
						list = list ::: List(comm.commitSha1)
						//}
					//}
				}
		} catch {
			case x: Throwable => {
				println("### Error in XMPPActor: " + x)
				if (debug) println(x.printStackTrace)
			}
		} finally {
			if (odb != null) { 
				odb.close
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
						var currentUserPreferences: String = ""
						prefs.getl("users").foreach{ 
							e => if (e("user") == element.getParticipant) currentUserPreferences = e("params")
						}
						val a = currentUserPreferences.split(' ')
						// XXX: only and max 3 arguments max:
						val showCommand = Array("git",  "--git-dir="+ repositoryDir +"","show", a(0), a(1), a(2), commitSha)
						val output = CommandExec.cmdExec(showCommand)
						if (debug)
							println("*** sent message length: " + output.length)
						element.sendMessage(output)
					}
				} catch {
					case e: Throwable => {
						println("### Error " + e + "\nTrying to put commit onto list cause errors.")
						DbAddCommit.writeCommitToDataBase(new Commit(commitSha))
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
							if (debug) println("*** received Quit command.")
							exit
						}
						case 'InitConnection => {
							if (debug) println("*** received InitConnection command.")
							initConnection
							act
						}
						case 'CloseConnection => {
							if (debug) println("*** received CloseConnection command.")
							closeConnection
						}
						case 'ProcessMessages => {
							tryToSendMessages
							act
						}
						case _ => {
							if (debug) println("*** received Unknown command.")
							act
						}
					}
				case _ => {
					act
				}
			}
		}
	}
	
}