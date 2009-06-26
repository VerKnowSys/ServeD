// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import prefs.PreferencesActor
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
			IRCActor ! 'Quit
			ScalaBot ! 'Quit
			println("Done\n")
		}
	})
	
	def main(args: Array[String]) {
		if (args.size < 1)
            arguments = Array("./") // set current dir if there's no given path to bot dir
        else
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
		
		IRCActor.start
		
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