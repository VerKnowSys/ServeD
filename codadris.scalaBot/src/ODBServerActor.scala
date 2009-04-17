// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import commiter._

import scala.actors._

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria._
import org.neodatis.odb.core.query.criteria._
// import org.neodatis.odb.Configuration


object ODBServerActor extends Actor {
	
	private var server: ODBServer = null
	// private var serverForIRC: ODBServer = null
	private var absolutePathToBotODB = ""
	private var prefs: Preferences = null
	private var debug = true
	
	def initServer = {
		try {
			Configuration.setDatabaseCharacterEncoding( "UTF8" )
			server = ODBFactory.openServer(prefs.geti("ODBPort"))
			server.addBase(prefs.get("ODBName"), absolutePathToBotODB + prefs.get("databaseName"))
			server.startServer(false) //start server in current thread
			
			// serverForIRC = ODBFactory.openServer(50604) // XXX hardcoded values
			// 		serverForIRC.addBase("ircLinksODB", absolutePathToBotODB + "ircLinksODB")
			// 		serverForIRC.startServer(true) //start server in new thread
			// 		
		} catch {
			case x: Throwable => {
				println("### Error: exception occured in ODBServerActor!")
				if (debug) println( x.printStackTrace )
			}
		} finally {
			if (server != null) {
				server.close
			}
			// if (serverForIRC != null) {
				// serverForIRC.close
			// }
		}
	}
	
	override def act = {
		Actor.loop {
			react {
				case (a: Preferences) => {
					prefs = a
					debug = prefs.getb("debug")
					initServer
					act
				}
				case 'Quit => {
					if (debug) println("*** ODBServer received Quit command.")
					if (server != null) server.close
					exit
				}
				case args: Array[String] => {
					if (debug) println("*** ODBServerActor recived arguments: " + args)
					absolutePathToBotODB = args(0)
					act
				}
			}
		}
	}	
}
