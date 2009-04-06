// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import scala.actors._

object ScalaBot extends Actor {
	
	private var arguments: Array[String] = Array()
	
	Runtime.getRuntime.addShutdownHook( new Thread {
		override def run = {
			println ("Bot shutdown requested.")
			XMPPActor ! 'CloseConnection
			XMPPActor ! 'Quit
			ODBServerActor ! 'Quit
			PreferencesActor ! 'Quit
		}
	})
	
	def main(args: Array[String]) {
		arguments = args
		println("Initializing..")
		this.start
	}
	
	override def act = {
		PreferencesActor.start
		PreferencesActor ! arguments
		
		ODBServerActor.start
		ODBServerActor ! arguments
		PreferencesActor ! 'ODBServerActorNeedPreferences
		Thread sleep 100 // XXX: These sleeps might be rewriten smarter i suppose. But actually I don't know how ;}
		ODBServerActor ! 'InitServer
		
		XMPPActor.start
		PreferencesActor ! 'XMPPActorNeedPreferences
		Thread sleep 100
		XMPPActor ! 'InitConnection
		
		Thread sleep 2500
		println("Ready to serve. waiting for orders.")
		Actor.loop {
			Thread sleep 500
			XMPPActor ! 'ProcessMessages
		}
	}
	
}