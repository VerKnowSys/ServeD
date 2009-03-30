package scalabot

import scala.actors._
import org.jivesoftware.smack._
// import org.jivesoftware.smack.XMPPConnection

object XMPPActor extends Actor {

	private val config = new ConnectionConfiguration("drakor.eu", 5222)
	private val connection = new XMPPConnection(config)	
	
	def initConnection = {
		config.setCompressionEnabled(true)
		//config.setSASLAuthenticationEnabled(true)
		connection.connect
		connection.login("git-bot@drakor.eu", "git-bot-666", "scalaBot")
	}
	
	def closeConnection = {
		// Disconnect from the server
		connection.disconnect
	}
	
	override def act = {
		while(true) {
			receive {
				case x: String =>
					println("received message: "+ x)
				case y: Symbol =>
					println("received command symbol: "+ y)
					if (y.equals(Symbol("Quit"))) {
						println("received quit command.")
						exit
					}
					if (y.equals(Symbol("InitConnection"))) {
						println("received init connection command")
						initConnection
					}
			}
		}
	}
	
}