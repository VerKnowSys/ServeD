// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot



import org.neodatis.odb.{OdbConfiguration, ODBServer, ODBFactory}
import signals.{Init, Quit}
import org.apache.log4j.Logger
import prefs.Preferences
import scala.actors._

object ODBServerActor extends Actor {
	
	private val prefs = ScalaBot.prefs
	private val logger = Logger.getLogger(ODBServerActor.getClass)
	private val debug = prefs.getb("debug")
	private val absolutePathToBotODBDir = System.getProperty("user.home") + "/" + ".svd/"
	private var server: ODBServer = null

	def initServer = {
		try {
			OdbConfiguration.setAutomaticCloseFileOnExit(true)
			OdbConfiguration.setDatabaseCharacterEncoding( "UTF8" )
			server = ODBFactory.openServer(prefs.geti("databaseODBPort"))
			server.addBase(prefs.get("xmppDatabaseName"), absolutePathToBotODBDir + prefs.get("xmppDatabaseFileName"))
			server.addBase(prefs.get("ircDatabaseName"), absolutePathToBotODBDir + prefs.get("ircDatabaseFileName"))
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
				case Init => {
					initServer
					act
				}
				case Quit => {
					logger.debug("*** ODBServer received Quit command.")
					if (server != null) server.close
					exit
				}
			}
		}
	}	
}
