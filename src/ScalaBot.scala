package scalabot

import scala.actors._

object ScalaBot extends Application {
	
	println("ScalaBot initializing..")
	
	XMPPActor.start
	XMPPActor ! 'InitConnection
	XMPPActor ! 'CloseConnection
	XMPPActor ! 'Quit
	
	CommandActor.start	
	CommandActor ! 'Quit
	
/*	CommandActor ! 'Quit
	XMPPActor ! 'Quit
*/	
	
	println("ScalaBot initialized and running..")
	
}