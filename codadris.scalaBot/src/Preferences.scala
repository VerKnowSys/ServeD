// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved. 
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import scala.collection.mutable.HashMap
import java.io._
import scala.xml.XML


sealed class Preferences {

	var value = HashMap[String,Any] (
		"debug" -> true,
		"resource" -> "scalaBot_0.6",
		"login" -> "git-bot",
		"password" -> "git-bot-666,",
		"server" -> "drakor.eu",
		"port" -> 5222,
		"databaseName" -> "/home/verknowsys/JAVA/ScalaBot/codadris.scalaBot/ScalaBotCommitDataBase.neodatis",
		"repositoryDir" -> "/git/codadris.git",
		"users" -> List(
			// XXX: only three arguments in Preferences:
			HashMap( "user" -> "dmilith@drakor.eu", "params" -> "--numstat --no-merges --no-merges" ),
			HashMap( "user" -> "szymon@jez.net.pl", "params" -> "--full-diff --numstat --no-merges" ),
			HashMap( "user" -> "karolrvn@jabber.verknowsys.info", "params" -> "--numstat --no-merges --no-merges" ),
			HashMap( "user" -> "vara@jabber.verknowsys.info", "params" -> "--numstat --no-merges --no-merges" )
		),
		"configFile" -> "scalaBot.config"
	)
	
	def toXML = 
		<preferences> 
			<debug>{value("debug")}</debug> 
			<resource>{value("resource")}</resource> 
			<login>{value("login")}</login> 
			<password>{value("password")}</password> 
			<server>{value("server")}</server> 
			<port>{value("port")}</port> 
			<databaseName>{value("databaseName")}</databaseName>
			<repositoryDir>{value("repositoryDir")}</repositoryDir>
			<users>
			{
				val list = value("users").asInstanceOf[List[HashMap[String,String]]]
				for( i <- list)
				yield
				<user>
					<name>{i("user")}</name>
					<params>{i("params")}</params>
				</user>
			}
			</users>
		</preferences> 
	
	// def fromXML(node: Node): Preferences = 
	// 	new Preferences {
	// 		val debug = (node \ "description").text 
	// 		val yearMade = (node \ "yearMade").text.toInt 
	// 		val dateObtained = (node \ "dateObtained").text 
	// 		val bookPrice = (node \ "bookPrice").text.toInt 
	// 		val purchasePrice = (node \ "purchasePrice").text.toInt 
	// 		val condition = (node \ "condition").text.toInt 
	// 	
	// 	} 
	// 
	
	def get(param: String): String = {
		return value(param).asInstanceOf[String]
	}
	
	def geti(param: String): Int = {
		return value(param).asInstanceOf[Int]
	}
	
	def getb(param: String): Boolean = {
		return value(param).asInstanceOf[Boolean]
	}
	
	def getl(param: String): List[HashMap[String,String]] = {
		return value(param).asInstanceOf[List[HashMap[String,String]]]
	}
	
	def loadPreferences: Preferences = {
		var sett = new Preferences
		try {
			// sett.value = Yaml.load(new File(sett.get("configFile").asInstanceOf[String])).asInstanceOf[HashMap[String,Any]]
		} catch {
			case x: Throwable => {
				println("*** config file "+sett.get("configFile")+" doesn't exists! creating new one")
				sett.savePreferences
			}
		}
		sett
	}

	def loadPreferences( configFileName: String ): Preferences = {
		var sett = new Preferences
		try {
			// sett.value = Yaml.load(configFileName).asInstanceOf[HashMap[String,Any]]
		} catch {
			case x: Throwable => {
				println("*** config file "+configFileName+" doesn't exists! creating new one")
				sett.savePreferences
			}
		}
		sett
	}
	
	def savePreferences = {
		// var matrix = Array[Array[String]]()
		// 	for(o <- value.keys) {
		// 		o match {
		// 			case x: String => {
		// 				matrix += Array[String](o, value.get(o))
		// 			}
		// 		}
		// 	}
		// 	// Yaml.dump(matrix, new File(value("configFile").asInstanceOf[String]))
	}
	
	def savePreferences(fileName: String) = ""// Yaml.dump(value, new File(fileName))

	// var settings: Settings = loadPreferences
	
}
