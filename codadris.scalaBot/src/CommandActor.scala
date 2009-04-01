// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.

package scalabot

import scala.actors._
import java.util._

object CommandActor extends Actor {

	val debug = ScalaBot.debug
	
	def initAndCheckVCS = {
		// TODO: add some requirements
	}
	
	def checkForNewMessages = {
		var commitData: Commit = null
		if (debug) {
			println("*** checkForNewMessages: Trying to send message")
		}
		try {
			// TODO: really do checking data to be sent to users
			commitData = new Commit(
			"Something was changed \nhere and\nin file \"somefile\"", "master256-" + new Date) // FIXME: hardcoded message
		} catch {
			case x: Throwable => {
				if (debug) {
					println("### Error: " + x)
				}
			}
		}
		XMPPActor ! commitData
	}
	
	override def act = {
		Actor.loop {
			react {
				case x: String => {
					println("received message: " + x)
					act
				}
				case y: Symbol => {
					y match {
						case 'Quit => {
							if (debug) println("received quit command.")
							exit
						}
						case 'CheckForNewMessages => {
							checkForNewMessages
						}
						case 'InitAndCheckVCS => {
							initAndCheckVCS
						}
					}
					act	
				}
			}
		}
	}
	
}
