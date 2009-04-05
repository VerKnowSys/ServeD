// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved. 
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import scala.collection.mutable.HashMap
import java.io._
import scala.xml.XML


sealed class Preferences {

	var value = HashMap[String,Any] (
		"debug" -> true,
		"resource" -> "scalaBot_0.7",
		"login" -> "git-bot",
		"password" -> "git-bot-666",
		"server" -> "drakor.eu",
		"port" -> 5222,
		"repositoryDir" -> "/git/codadris.git",
		"users" -> List(
			// XXX: only three arguments in Preferences:
			HashMap( "user" -> "dmilith@drakor.eu", "params" -> "--numstat --no-merges --no-merges" ),
			HashMap( "user" -> "szymon@jez.net.pl", "params" -> "--full-diff --numstat --no-merges" ),
			HashMap( "user" -> "karolrvn@jabber.verknowsys.info", "params" -> "--numstat --no-merges --no-merges" ),
			HashMap( "user" -> "vara@jabber.verknowsys.info", "params" -> "--numstat --no-merges --no-merges" )
		),
		"configFile" -> "scalaBotConfig.xml",
		"statusDescription" -> "I should work fine like a death spell!",
		"absoultePathToBotODB" -> "/home/verknowsys/JAVA/ScalaBot/codadris.scalaBot/",
		"databaseName" -> "ScalaBotCommitDataBase.neodatis",
		"ODBPort" -> 50603,
		"ODBName" -> "scalaBotCommitDatabase",
		"ODBListenAddress" -> "127.0.0.1"
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
			<absoultePathToBotODB>{value("absoultePathToBotODB")}</absoultePathToBotODB>
			<repositoryDir>{value("repositoryDir")}</repositoryDir>
			<statusDescription>{value("statusDescription")}</statusDescription>
			<ODBPort>{value("ODBPort")}</ODBPort>
			<ODBName>{value("ODBName")}</ODBName>
			<ODBListenAddress>{value("ODBListenAddress")}</ODBListenAddress>
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
	
	def fromXML(node: scala.xml.Node): HashMap[String,Any] = {
		var hashMap = HashMap[String,Any]()
			hashMap.update( "debug", (node \ "debug").text.toBoolean)
			hashMap.update( "resource", (node \ "resource").text)
			hashMap.update( "login", (node \ "login").text)
			hashMap.update( "password", (node \ "password").text)
			hashMap.update( "server", (node \ "server").text)
			hashMap.update( "port", (node \ "port").text.toInt)
			hashMap.update( "databaseName", (node \ "databaseName").text)
			hashMap.update( "absoultePathToBotODB", (node \ "absoultePathToBotODB").text)
			hashMap.update( "repositoryDir", (node \ "repositoryDir").text)
			hashMap.update( "statusDescription", (node \ "statusDescription").text)
			hashMap.update( "ODBPort", (node \ "ODBPort").text)
			hashMap.update( "ODBName", (node \ "ODBName").text)
			hashMap.update( "ODBListenAddress", (node \ "ODBListenAddress").text)
			var list = List[HashMap[String,String]]()
			(node \\ "user").foreach { user =>
				user.foreach { nod =>
					val name = (nod \ "name").text
					val params = (nod \ "params").text
					list = list ::: List( HashMap( "user" -> name, "params" -> params ) ).asInstanceOf[List[HashMap[String,String]]]
				}
			}
			hashMap.update( "users", list )
		hashMap.asInstanceOf[HashMap[String,Any]]
	} 
	
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
			val loadnode = xml.XML.loadFile(sett.get("absoultePathToBotODB") + sett.get("configFile")) 
			sett.value = fromXML(loadnode)
		} catch {
			case x: Throwable => {
				println("*** config file " + sett.get("absoultePathToBotODB") + sett.get("configFile")+" doesn't exists! creating new one")
				sett.savePreferences
			}
		}
		sett
	}

	def loadPreferences( configFileName: String ): Preferences = {
		var sett = new Preferences
		try {
			val loadnode = xml.XML.loadFile(sett.get("absoultePathToBotODB") + sett.get("configFile")) 
 			sett.value = fromXML(loadnode)
		} catch {
			case x: Throwable => {
				println("*** config file " + sett.get("absoultePathToBotODB") + configFileName + " doesn't exists! creating new one")
				sett.savePreferences
			}
		}
		sett
	}
	
	def savePreferences = scala.xml.XML.saveFull( get("absoultePathToBotODB") + get("configFile"), this.toXML, "UTF-8", true, null)
	
	def savePreferences(fileName: String) = scala.xml.XML.saveFull(fileName, this.toXML, "UTF-8", true, null)
	
}
