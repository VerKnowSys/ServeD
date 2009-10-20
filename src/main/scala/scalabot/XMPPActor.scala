// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot


import commiter.{Commit, DbAddCommit}
import signals.{Init, Quit, ProcessMessages, MainLoop}
import command.exec.CommandExec
import org.apache.log4j.Logger
import prefs.Preferences
import scala.actors._

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria._
import org.neodatis.odb.core.query.criteria._

import org.jivesoftware.smack._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.filter._


// TODO: to be refactored - Preferences should be parametrized
object XMPPActor extends Actor with MessageListener { 

	lazy val prefs: Preferences = new Preferences
	lazy val logger = Logger.getLogger(XMPPActor.getClass)
	lazy val debug = prefs.getb("debug")
	lazy val config = new ConnectionConfiguration(prefs.get("xmppServer"), prefs.geti("xmppPort"))
	lazy val connection = new XMPPConnection(config)
	lazy val presence = new Presence(Presence.Type.available)
	lazy val login = prefs.get("xmppLogin")
	lazy val password = prefs.get("xmppPassword")
	lazy val resource = prefs.get("xmppResourceString")
	lazy val gitRepositoryProjectDir = prefs.get("gitRepositoryProjectDir")

	private var filter: AndFilter = null
	private var chatmanager: ChatManager = null
	private var chat: List[Chat] = List()
	
	def initConnection = {
		XMPPConnection.DEBUG_ENABLED = false // NOTE: 2009-10-20 17:12:58 - dmilith - please look out for this bullshitting method added by some stupid fucks with requirement for X11 display
		config.setCompressionEnabled(true)
		config.setSASLAuthenticationEnabled(false)
		connection.connect()
		logger.debug("*** l:"+login + " p:" + password + " r:" + resource)
		try {
			connection.login(login, password, resource)
		} catch {
			case x: Throwable => {
				logger.error("### Error while connecting to XMPP server. Please check login / password.")
				logger.debug( x.printStackTrace )
                exit
			}
		}
		chatmanager = connection.getChatManager
		logger.debug("*** num of users: " + chat.length)
		prefs.getlh("users").foreach { x =>
			try {
				chat ::= chatmanager.createChat(x("user"), this)
			} catch {
				case x: Throwable => {
					if (debug) {
						logger.info("### Error: " + x )
					}
				}
			}
		}
		logger.debug("*** num of users: " + chat.length)
		presence.setStatus(prefs.get("xmppStatusDescription"))
		presence.setMode(Presence.Mode.dnd)
		connection.sendPacket(presence)
		logger.debug("*** Connected as: " + login + "\nReady to enter main loop")
		ScalaBot ! MainLoop
	}

	def processMessage(chat: Chat, message: Message) {
		logger.debug("*** Received message: " + message + " (\"" + message.getBody + "\")")
//		prefs.getlh("users").foreach { x =>
//			x("user")
//		}
		if (message.getFrom.contains("dmilith") || message.getFrom.contains("vara")) {   // XXX: hardcoded value
			logger.info("Message contains dmilith: " + message.getFrom)
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
		OdbConfiguration.setAutomaticCloseFileOnExit(true)
		OdbConfiguration.setDatabaseCharacterEncoding( "UTF8" )
		try {
		    odb = ODBFactory.openClient(prefs.get("xmppDatabaseListenAddress"), prefs.geti("databaseODBPort"), prefs.get("xmppDatabaseName"))
		    var query = new CriteriaQuery(classOf[Commit], Where.equal("toRead", true))
			query.orderByDesc("date") 
			val commit = odb.getObjects(query)
				while (commit.hasNext) {
					val comm = (commit.next).asInstanceOf[Commit]
						comm.toRead = false
						odb.store(comm)
						if (debug)
							logger.info("*** Found in database: " + comm.commitSha1)
						list = list ::: List(comm.commitSha1)
				}
		} catch {
			case x: Throwable => {
				logger.info("### Error in XMPPActor: " + x)
				logger.debug(x.printStackTrace)
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
							logger.info("*** Trying to send messages, to User: " + element.getParticipant)
						}
						var currentUserPreferences: String = ""
						prefs.getlh("users").foreach{
							e => if (e("user") == element.getParticipant) currentUserPreferences = e("params")
						}
						val git = prefs.get("gitExecutable")
						// NOTE: ListBuffer provides append method, and it should be used for large Lists
						val showCommand = List(git, "--git-dir=" + gitRepositoryProjectDir, "show") ++ currentUserPreferences.split(' ') ++ List(commitSha)
						val output = CommandExec.cmdExec(showCommand.toArray)
						if (debug)
							logger.info("*** sent message length: " + output.length)
						element.sendMessage(output)
					}
				} catch {
					case e: Throwable => {
						logger.info("### Error " + e + "\nTrying to put commit onto list cause errors.")
						DbAddCommit.writeCommitToDataBase(new Commit(commitSha))
					}
				}
			}
		}
	}
		
	override def act = {
		Actor.loop {
			react {
				case Init => {
					initConnection // init connection after getting preferences
					act
				}
				case Quit => {
					logger.debug("*** received Quit command, closing connection with XMPP server.")
					closeConnection
					exit
				}
				case ProcessMessages => {
					tryToSendMessages
					act
				}
				case _ => {
					logger.debug("*** received Unknown command.")
					act
				}
			}
		}
	}
	
}