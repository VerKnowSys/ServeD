// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import scala.actors._

object ScalaBot extends Application with Actor {
	
	private val prefs = (new Preferences).loadPreferences
	private val debug = prefs.getb("debug")
	
	Runtime.getRuntime.addShutdownHook( new Thread {
		override def run = {
			println ("bot shutdown requested.")
			XMPPActor ! 'CloseConnection
			XMPPActor ! 'Quit
			ODBServerActor ! 'Quit
		}
	})
	
	println("initializing..")
	this.start
	
	override def act = {
		ODBServerActor.start
		ODBServerActor ! 'InitServer
		
		XMPPActor.start
		XMPPActor ! 'InitConnection
		
		Thread sleep 2500
		println("ready to serve. waiting for orders.")
		Actor.loop {
			Thread sleep 500
			XMPPActor ! 'ProcessMessages
		}
	}
	
}