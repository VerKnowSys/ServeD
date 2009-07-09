// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import prefs.Preferences
import signals.Quit
import org.apache.log4j.Logger
import scala.actors._
import scala.actors.Actor._
import org.jibble.pircbot._
import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria._


object IRCActor extends PircBot with Actor {

	private val logger = Logger.getLogger(IRCActor.getClass)
	private val prefs = (new Preferences).loadPreferences
	
	def settings = {
		setVerbose(prefs.getb("ircDebugInfo"))
		setName(prefs.get("ircName"))
		setAutoNickChange(prefs.getb("ircAutoNickChange"))
		setVersion(prefs.get("ircVersionString"))
		setEncoding(prefs.get("ircEncoding"))
		connect(prefs.get("ircServer"))
		for (i <- prefs.getl("ircAutoJoinChannels"))
			joinChannel(i)
	}

	override def act = {
		settings // TODO: XXX: IRC Actor is inactive ATM, it's useless now, maybe we could do something with it later. Maybe logging of irc chats?
		react {
			case Quit => {
				disconnect
			}
		}
	}
	
	def getLinks(howMany: Int): List[LinkInfo] = {
		var odb: ODB = null
		var list: List[LinkInfo] = List()
		try {
		    odb = ODBFactory.openClient(prefs.get("ircDatabaseListenAddress"), prefs.geti("databaseODBPort"), prefs.get("ircDatabaseName"))
		    val query = new CriteriaQuery(classOf[LinkInfo]) //, Where.equal("date.getDay", (new Date).getDay))
			val link = odb.getObjects(query.orderByDesc("date"))
				while (link.hasNext && (list.size <= howMany)) {
					val comm = (link.next).asInstanceOf[LinkInfo]
					list ::= comm
				}
		} catch {
			case x: Throwable => {
				logger.info("### Error in getLinks: " + x)
				logger.info(x.printStackTrace)
			}
		} finally {
			if (odb != null) { 
				odb.close
			} 
		}
		return list
	}
	
	def putLinkToDatabase(arg: LinkInfo) {
		var odb: ODB = null
		try {
			odb = ODBFactory.openClient(prefs.get("ircDatabaseListenAddress"), prefs.geti("databaseODBPort"), prefs.get("ircDatabaseName"))
			odb.store( arg )
			odb.commit
		} catch {
			case x: Throwable => {
				logger.info("### Error: There were problems while connecting to ODB server.")
			}
		} finally {
			if (odb != null) { 
				odb.close
			} 
		}
	}
	
	override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) {
		if (message.contains("http://") || message.contains("www.")) {
			val link = new LinkInfo(sender, channel, "\"" + message + "\"")
			putLinkToDatabase(link)
		}
		try {
			if (message.split(' ')(0).equalsIgnoreCase("!links") && message.split(' ')(1).length > 2) {
				sendMessage( sender, "You requested, to find links which contain: \"" + message.split(' ')(1) + "\"…" )
				var msg = ""
				for (link <- getLinks(10000)) { // XXX hardcoded max of 10.000 links to search in
					if (link.message.toUpperCase.contains(message.split(' ')(1).toUpperCase)) {
						msg = "On: " + link.channel + " @(" + link.date.toString + "), by " + link.author + ": " + link.message
						sendMessage( sender, msg )
						Thread.sleep(1000)
					}
				}
			}
		} catch {
			case x: Throwable => {
				sendMessage( sender, "Taking 10 last links with their context:" )
				var msg = ""
				for (link <- getLinks(10)) {
					msg = "On: " + link.channel + " @(" + link.date.toString + ", by " + link.author + ": " + link.message
					sendMessage( sender, msg )
					Thread.sleep(1000)
				}
			}
		}
	}
	
	override def onDisconnect = {
		try {
			settings
		} catch {
			case x: Throwable => {
				logger.info("### Disconnected and cannot connect again! I'll keep trying.. " + x.getMessage)
				settings
			}
		}
	}
	
}