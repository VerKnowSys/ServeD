// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import command.exec.CommandExec
import commiter.DbAddCommit
import prefs.Preferences
import scala.actors._

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria._
import org.neodatis.odb.core.query.criteria._

import org.jivesoftware.smack._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.filter._
import org.jivesoftware.smackx._



object XMPPActor extends Actor with MessageListener { // with PacketListener 
	
	private var prefs: Preferences = null
	private var debug = true
	private var config: ConnectionConfiguration = null
	private var connection: XMPPConnection = null
	private var presence: Presence = null
	private var login = ""
	private var password = ""
	private var resource = ""
	private var repositoryDir = ""

	private var filter: AndFilter = null
	private var chatmanager: ChatManager = null
	private var chat: List[Chat] = List()
	
	def initConnection = {
		XMPPConnection.DEBUG_ENABLED = true
		config.setCompressionEnabled(true)
		config.setSASLAuthenticationEnabled(false)
		connection.connect()
		if (debug) println("*** l:"+login + " p:" + password + " r:" + resource)
		try {
			connection.login(login, password, resource)
		} catch {
			case x: Throwable => {
				println("### Error while connecting to XMPP server. Please check login / password.")
				if (debug) println( x.printStackTrace )
                exit
			}
		}
		chatmanager = connection.getChatManager
		if (debug) println("*** num of users: " + chat.length)
		prefs.getlh("users").foreach { x =>
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
		presence.setMode(Presence.Mode.dnd)
		connection.sendPacket(presence)
		if (debug) println("*** Connected as: " + login + "\nReady to enter main loop")
		ScalaBot ! 'MainLoop
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
		if (message.getFrom.contains("dmilith")) {   // XXX: hardcoded value
			message.getBody match {
				case "last" => {
					chat.sendMessage("Requested last commit.\nNYI")
				}
				case "last5" => {
					chat.sendMessage("Requested last 5 commits.\nNYI")
				}
				case "last10" => {
					chat.sendMessage("Requested last 10 commits.\nNYI")
				}
				case "help" => {
					chat.sendMessage("No help for noobs ;}")
				}
			}
		}
	}
	
	def closeConnection = {
		connection.disconnect
	}
	
	def getMessages: List[String] = {
		var odb: ODB = null
		var list: List[String] = List()
		try {
		    odb = ODBFactory.openClient(prefs.get("ODBListenAddress"), prefs.geti("ODBPort"), prefs.get("ODBName"))
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
					val comm = (commit.next).asInstanceOf[Commit]
						comm.toRead = false
						odb.store(comm)
						if (debug)
							println("*** Found in database: " + comm.commitSha1)
						list = list ::: List(comm.commitSha1)
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
						prefs.getlh("users").foreach{
							e => if (e("user") == element.getParticipant) currentUserPreferences = e("params")
						}
						val a = currentUserPreferences.split(' ')
						// XXX: only and max 3 arguments max:
						val git = prefs.get("gitExecutable")
						val showCommand = Array(git,  "--git-dir="+ repositoryDir +"","show", a(0), a(1), a(2), commitSha)
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
				case s: Preferences => {
					prefs = s
					debug = prefs.getb("debug")
					config = new ConnectionConfiguration(prefs.get("server"), prefs.geti("port"))
					connection = new XMPPConnection(config)	
					presence = new Presence(Presence.Type.available)
					login = prefs.get("login")
					password = prefs.get("password")
					resource = prefs.get("resource")
					repositoryDir = prefs.get("repositoryDir")
					initConnection // init connection after getting preferences
					act
				}
				case y: Symbol =>
					y match {
						case 'Quit => {
							if (debug) println("*** received Quit command.")
							exit
						}
						case 'CloseConnection => {
							if (debug) println("*** received CloseConnection command.")
							closeConnection
							act
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
			}
		}
	}
	
}