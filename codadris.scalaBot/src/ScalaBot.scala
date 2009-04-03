// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import scala.actors._

object ScalaBot extends Application with Actor {
	
	val debug = true
	val databaseName = "../ScalaBotCommitDataBase.neodatis"
	
	Runtime.getRuntime.addShutdownHook( new Thread {
		override def run = {
			println ("Called at shutdown.")
			XMPPActor ! 'Quit
		}
	})
	
	println("ScalaBot initializing..")
	this.start
	
	override def act = {
		XMPPActor.start
		XMPPActor ! 'InitConnection
		
		Thread sleep 2500
		println("Ready to serve. Waiting for orders.")
		Actor.loop {
			Thread sleep 500
			XMPPActor ! 'ProcessMessages
		}
	}
	
}