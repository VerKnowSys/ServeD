// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved. 
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import scala.collection.immutable.HashMap

object Settings {
	
	val debug = true
	
	def getUsers = {
		List(
			// XXX: only three arguments in settings:
			HashMap( "user" -> "dmilith@drakor.eu", "settings" -> "--numstat --no-merges --no-merges" ),
			HashMap( "user" -> "szymon@jez.net.pl", "settings" -> "--full-diff --numstat --no-merges" ),
			HashMap( "user" -> "karolrvn@jabber.verknowsys.info", "settings" -> "--numstat --no-merges --no-merges" ),
			HashMap( "user" -> "vara@jabber.verknowsys.info", "settings" -> "--numstat --no-merges --no-merges" )
		)
	}
	
	val databaseName = "/home/verknowsys/JAVA/ScalaBot/codadris.scalaBot/ScalaBotCommitDataBase.neodatis"
	val repositoryDir = "/git/codadris.git"
	val resource = "scalaBot_0.5"
	
	val login = "git-bot"
	val password = "git-bot-666"
	val server = "drakor.eu"
	val port = 5222
	
}