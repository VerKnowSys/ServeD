package scalabot

import scala.actors._

object ScalaBot extends Application with Actor {
	
	val debug = true
	
	println("ScalaBot initializing..")
	this.start
	
	override def act = {
		XMPPActor.start
		XMPPActor ! 'InitConnection
		CommandActor.start
		CommandActor ! 'InitAndCheckVCS
		
		Thread sleep 2000
		Actor.loop {
			Thread sleep 1000
			CommandActor ! 'CheckForNewMessages
		}
	}
	
}