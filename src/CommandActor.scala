package scalabot

import scala.actors._

object CommandActor extends Actor {

	override def act() {
		while(true) {
			receive {
				case x: String => {
					println("received message: "+ x)
					CommandActor ! "Do command"
				}
				case y: Symbol =>
					println("received command symbol: "+ y)
					if (y.equals(Symbol("Quit"))) {
						println("received quit command.")
						exit
					}
			}
		}
	}
	
}
