// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved. 
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import scala.collection.immutable.HashMap
import org.ho.yaml._
import java.io._

class Settings { // Preferences with default parametters
	var debug = true
	var resource = "scalaBot_0.5"
	var login = "git-bot"
	var password = "git-bot-666"
	var server = "drakor.eu"
	var port = 5222
	var databaseName = "/home/verknowsys/JAVA/ScalaBot/codadris.scalaBot/ScalaBotCommitDataBase.neodatis"
	var repositoryDir = "/git/codadris.git"
	var users = List(
		// XXX: only three arguments in Preferences:
		HashMap( "user" -> "dmilith@drakor.eu", "Preferences" -> "--numstat --no-merges --no-merges" ),
		HashMap( "user" -> "szymon@jez.net.pl", "Preferences" -> "--full-diff --numstat --no-merges" ),
		HashMap( "user" -> "karolrvn@jabber.verknowsys.info", "Preferences" -> "--numstat --no-merges --no-merges" ),
		HashMap( "user" -> "vara@jabber.verknowsys.info", "Preferences" -> "--numstat --no-merges --no-merges" )
	)
}


object Preferences {

	val botConfigFile = "/home/verknowsys/Java/ScalaBot/codadris.scalaBot/ScalaBot.config"
	
	def loadPreferences = {
		try {
			settings = Yaml.loadType(new File(botConfigFile), classOf[Settings])
		} catch {
			case x: Throwable => {
				println("*** config file "+botConfigFile+" doesn't exists! creating new one")
				savePreferences(new Settings)
				settings = new Settings
			}
		}
		settings
	}
	
	def savePreferences(set: Settings) = Yaml.dump(set, new File(botConfigFile))

	var settings: Settings = loadPreferences
	
}
