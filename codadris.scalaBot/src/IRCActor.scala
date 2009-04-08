// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import scala.actors._
import scala.actors.Actor._
import scala.collection.mutable.HashMap

import java.util.Date
import org.jibble.pircbot._

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria._
import org.neodatis.odb.core.query.criteria._


object IRCActor extends PircBot with Actor {

	def settings = {
		this.setVerbose(false)
		this.setVersion("ScalaBot based on pircbot")
		this.setEncoding("UTF-8")
		this.connect("irc.freenode.net")
		this.joinChannel("#scala.pl")
		this.joinChannel("#ruby.pl")
		this.joinChannel("#scala")
	}

	override def act = {
		try {
			this.setName("ScalaBot")
			settings
		} catch {
			case v: Throwable => {
				this.setName("ScalaBot-")
				settings
			}
		}
		react {
			case 'Quit => {
				this.disconnect
			}
		}
	}
	
	def getLinks(howMany: Int): List[LinkInfo] = {
		var odb: ODB = null
		var list: List[LinkInfo] = List()
		try {
		    odb = ODBFactory.openClient("127.0.0.1", 50603, "scalaBotCommitDatabase")
		    var query = new CriteriaQuery(classOf[LinkInfo]) //, Where.equal("date.getDay", (new Date).getDay))
			query.orderByDesc("date")
			val link = odb.getObjects(query)
				while (link.hasNext && (list.size <= howMany)) {
					val comm = (link.next).asInstanceOf[LinkInfo]
					list = list ::: List(comm)
				}
		} catch {
			case x: Throwable => {
				println("### Error in getLinks: " + x)
				println(x.printStackTrace)
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
			odb = ODBFactory.openClient("127.0.0.1", 50603, "scalaBotCommitDatabase")
			odb.store( arg )
			odb.commit
		} catch {
			case x: Throwable => {
				println("### Error: There were problems while connecting to ODB server.")
			}
		} finally {
			if (odb != null) { 
				odb.close
			} 
		}
	}
	
	override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) {
		if (message.equalsIgnoreCase("!kawa")) {
			sendMessage(channel,sender + ": Taki problem ruszyć dupsko po kawę do automatu? ;P")
		}
		if (message.equalsIgnoreCase("!herbata")) {
			actor {
				Thread.sleep(240000)
				sendMessage(channel, sender + ": Minęły 4minuty. Herbata gotowa")
			}
		}
		if (message.contains("http://") || message.contains("www.")) {
			val link = new LinkInfo(sender, channel, "\"" + message + "\"")
			putLinkToDatabase(link)
		}
		try {
			if (message.split(' ')(0).equalsIgnoreCase("!links") && message.split(' ')(1).length > 2) {
				sendMessage( sender, "You requested, to find links which contain: \"" + message.split(' ')(1) + "\"…" )
				var msg = ""
				for (link <- getLinks(100000)) { // XXX hardcoded max of 100.000 links to search in
					if (link.message.toUpperCase.contains(message.split(' ')(1).toUpperCase)) {
						msg = "On: " + link.channel + " @(" + link.date.toString + "), by " + link.author +
						": " + link.message
						sendMessage( sender, msg )
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
				}
			}
		}
	}
	
	override def onDisconnect = {
		try {
			act
		} catch {
			case x: Throwable => {
				println("### Disconnected and cannot connect again! " + x.getMessage)
				act
			}
		}
	}
	
}