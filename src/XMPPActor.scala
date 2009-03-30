package scalabot

import scala.actors._
import org.jivesoftware.smack._

object XMPPActor extends Actor {

	private val config = new ConnectionConfiguration("drakor.eu", 5222)
	private val connection = new XMPPConnection(config)	
	
	def initConnection = {
		config.setCompressionEnabled(true)
		config.setSASLAuthenticationEnabled(false)
		connection.connect
		connection.login("git-bot", "git-bot-666", "scalaBot")
	}

	def closeConnection = {
		connection.disconnect
	}

	override def act = {
		while(true) {
			receive {
				case x: String =>
					println("received message: "+ x)
				case y: Symbol =>
					println("received command symbol: "+ y)
					y match {
						case 'Quit => {
							println("received Quit command.")
							exit
						}
						case 'InitConnection => {
							println("received InitConnection command.")
							initConnection
						}
						case 'CloseConnection => {
							println("received CloseConnection command.")
							closeConnection
						}
						case _ => {
							println("received Unknown command.")
						}
					}
			}
		}
	}
	
}