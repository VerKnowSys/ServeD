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
			ScalaBot ! 'Quit
			println("Done\n")
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
		
		XMPPActor.start
		PreferencesActor ! 'XMPPActorNeedPreferences
		
		react {
			case 'MainLoop => {
				Actor.loop {
					Thread sleep 500 // 500 ms for each check. That's enough even for very often updated repository
					XMPPActor ! 'ProcessMessages
				}
			}
			case 'Quit => {
				exit
			}
		}
		println("Ready to serve. waiting for orders.")
	}
	
}