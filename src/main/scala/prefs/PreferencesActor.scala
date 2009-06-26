// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package prefs

import deployer.Deployer
import scala.actors._
import scalabot.{ODBServerActor, XMPPActor}

object PreferencesActor extends Actor {
	
	var prefs: Preferences = null
	
	override def act = {
			Actor.loop {
				react {
					case z: Symbol => {
						z match {
							case 'ODBServerActorNeedPreferences => {
								ODBServerActor ! prefs
								act
							}
							case 'XMPPActorNeedPreferences => {
								XMPPActor ! prefs
								act
							}
							case 'DeployerNeedPreferences => {
								Deployer ! prefs
								act
							}
							case 'Quit => {
								exit
							}
						}
					}
					case x: Array[String] => {
						println ("Setting absolute git path to: " + x(0))
						prefs = new Preferences(x(0)).loadPreferences
						act
					}
					case (a: String, b: String) => {
						println ("*** setting key: " + a + " with value: " + b)
						prefs.value(a) = b
						act
					}
				}
			}
		}
		
}