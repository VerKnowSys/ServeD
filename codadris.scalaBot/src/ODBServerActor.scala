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
	
	private val prefs = (new Preferences).loadPreferences
	private val debug = prefs.getb("debug")	
	private val databaseName = prefs.get("databaseName")
	
	def initServer = {
		try { 
			server = ODBFactory.openServer(50603)
			server.addBase("commitDatabase", databaseName)
			server.startServer(false) //start server in current thread
		} finally {
			if (server != null) {
				server.close
			}
		}
	}
	
	override def act = {
		Actor.loop {
			react {
				case y: Symbol =>
					y match {
						case 'Quit => {
							if (debug) println("*** ODBServer received Quit command.")
							if (server != null) server.close
							exit
						}
						case 'InitConnection => {
							if (debug) println("*** ODBServer received InitServer command.")
							initServer
							act
						}
					}
			}
		}
	}	
}
