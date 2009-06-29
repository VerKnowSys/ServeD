// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import commiter._

import org.apache.log4j.Logger
import prefs.Preferences
import scala.actors._
import scala.actors.Actor._

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria._
import org.neodatis.odb.core.query.criteria._


object ODBServerActor extends Actor {
	
	private val prefs = (new Preferences).loadPreferences
	private val logger = Logger.getLogger(ODBServerActor.getClass)
	private val debug = prefs.getb("debug")
	private val absolutePathToBotODB = System.getProperty("user.dir") + "/"
	private var server: ODBServer = null

	def initServer = {
		try {
			Configuration.setDatabaseCharacterEncoding( "UTF8" )
			server = ODBFactory.openServer(prefs.geti("ODBPort"))
			server.addBase(prefs.get("ODBName"), absolutePathToBotODB + prefs.get("databaseName"))
			server.startServer(false) //start server in current thread
		} catch {
			case x: Throwable => {
				logger.info("### Error: exception occured in ODBServerActor!")
				logger.debug( x.printStackTrace )
			}
		} finally {
			if (server != null) {
				server.close
			}
		}
	}
	
 	override def act = {
		Actor.loop {
			react {
				case 'Init => {
					initServer
					act
				}
				case 'Quit => {
					logger.debug("*** ODBServer received Quit command.")
					if (server != null) server.close
					exit
				}
			}
		}
	}	
}
