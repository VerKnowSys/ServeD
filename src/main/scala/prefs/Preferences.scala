// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved. 
// This Software is a close code project. You may not redistribute this code without permission of author.

package prefs

import scala.collection.mutable.HashMap
import java.io._
import scala.xml.XML


sealed class Preferences(absolutePathToBot: String) {

	val absolutePathToBotODB = absolutePathToBot
	var value = HashMap[String,Any] (
		"debug" -> true,
		"resource" -> "scalaBot-2",
		"login" -> "varra",
		"password" -> "varrajabber",
		"server" -> "drakor.eu",
		"port" -> 5222,
		"gitExecutable" -> "/opt/local/bin/git",
		"repositoryDir" -> "/git/scala.project.tools.git/.git",
		"jarSignerPassword" -> "gru5zka.",
		"jarSignerExecutable" -> "/usr/bin/jarsigner",
		"jarSignerKeyName" -> "VerKnowSys",
		"sshUserName" -> "verknowsys",
		"sshPassword" -> "gru5zka.",
		"sshHost" -> "verknowsys.info",
		"sshPort" -> 22,
		"users" -> List(
			// XXX: only three arguments in Preferences:
			HashMap( "user" -> "dmilith@drakor.eu", "params" -> "--numstat --no-merges --no-merges" )
//			HashMap( "user" -> "szymon@jez.net.pl", "params" -> "--full-diff --numstat --no-merges" ),
//			HashMap( "user" -> "karolrvn@jabber.verknowsys.info", "params" -> "--numstat --no-merges --no-merges" ),
//			HashMap( "user" -> "vara@jabber.verknowsys.info", "params" -> "--numstat --no-merges --no-merges" )
		),
		"deployFilesBasic" -> List(
			"codadris.utils-0.0.1-SNAPSHOT.jar",
			"codadris.gui-0.0.1-SNAPSHOT.jar",
			"codadris.gui.utils-0.0.1-SNAPSHOT.jar",
			"codadris.gui.screenspace-0.0.1-SNAPSHOT.jar",
			"codadris.gui.suite-0.0.1-SNAPSHOT.jar",
			"codadris.gui.textedit-0.0.1-SNAPSHOT.jar",
			"codadris.gui.treetable-0.0.1-SNAPSHOT.jar",
			"codadris.gui.scala-0.0.1-SNAPSHOT.jar",
			"codadris.dbgui-0.0.1-SNAPSHOT.jar",
			"codadris.dbapp-0.0.1-SNAPSHOT.jar",
			"flexdock_codadris-0.0.1-SNAPSHOT.jar",
			"codadris.binblocklang-0.0.1-SNAPSHOT.jar"
		),
		"deployFilesAdditionalDependencies" -> List(
			"commons-io-1.4.jar",
			"commons-lang-2.4.jar",
			"commons-logging-1.1.1.jar",
			"log4j-1.2.14.jar",
			"junit-4.4.jar",
			"jcommon-1.0.12.jar",
			"jargs-0.0.1-SNAPSHOT.jar",
			"jfreechart-1.0.9.jar",
			"looks-2.1.2.jar",
			"net.jcip.annotations-0.0.1-SNAPSHOT.jar",
			"skinlf-1.2.3.jar",
			"swing-layout-1.0.jar",
			"swingx-0.9.5-2.jar",
			"filters-2.0.235.jar",
			"timingframework-1.0.jar",
			"scala-swing-2.7.5.jar",
			"scala-compiler-2.7.5.jar",
			"scala-library-2.7.5.jar"
		),
		"configFile" -> "project.tools.xml",
		"statusDescription" -> "I should work fine.",
		"databaseName" -> "ScalaBotCommitDataBase.neodatis",
		"ODBPort" -> 50604,
		"ODBName" -> "scalaBotCommitDatabase",
		"ODBListenAddress" -> "127.0.0.1",
		"remoteWebStartDeployDir" -> "/home/verknowsys/public_html/javaws/coviob2/dist/"
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
			<gitExecutable>{value("gitExecutable")}</gitExecutable>
			<jarSignerPassword>{value("jarSignerPassword")}</jarSignerPassword>
			<jarSignerExecutable>{value("jarSignerExecutable")}</jarSignerExecutable>
			<jarSignerKeyName>{value("jarSignerKeyName")}</jarSignerKeyName>
			<sshPassword>{value("sshPassword")}</sshPassword>
			<sshUserName>{value("sshUserName")}</sshUserName>
			<sshHost>{value("sshHost")}</sshHost>
			<sshPort>{value("sshPort")}</sshPort>
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
			<deployFilesBasic>
			{
				val list = value("deployFilesBasic").asInstanceOf[List[String]]
				for( i <- list)
				yield
				<file>
				{i}
				</file>
			}
			</deployFilesBasic>
			<deployFilesAdditionalDependencies>
			{
				val list = value("deployFilesAdditionalDependencies").asInstanceOf[List[String]]
				for( i <- list)
				yield
				<file>
				{i}
				</file>
			}
			</deployFilesAdditionalDependencies>
			<remoteWebStartDeployDir>{value("remoteWebStartDeployDir")}</remoteWebStartDeployDir>	
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
			hashMap.update( "gitExecutable", (node \ "gitExecutable").text)
			hashMap.update( "jarSignerPassword", (node \ "jarSignerPassword").text)
			hashMap.update( "jarSignerExecutable", (node \ "jarSignerExecutable").text)
			hashMap.update( "jarSignerKeyName", (node \ "jarSignerKeyName").text)
			hashMap.update( "sshPassword", (node \ "sshPassword").text)
			hashMap.update( "sshUserName", (node \ "sshUserName").text)
			hashMap.update( "sshHost", (node \ "sshHost").text)
			hashMap.update( "sshPort", (node \ "sshPort").text.toInt)
			hashMap.update( "repositoryDir", (node \ "repositoryDir").text)
			hashMap.update( "statusDescription", (node \ "statusDescription").text)
			hashMap.update( "ODBPort", (node \ "ODBPort").text.toInt)
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

			var list2 = List[String]()
			(node \\ "file").foreach { file =>
				list2 = list2 ::: List( file.text ).asInstanceOf[List[String]]
			}
			hashMap.update( "deployFilesBasic", list2 )

			list2 = List[String]()
			(node \\ "file").foreach { file =>
				list2 = list2 ::: List( file.text ).asInstanceOf[List[String]]
			}
			hashMap.update( "deployFilesAdditionalDependencies", list2 )

			hashMap.update( "remoteWebStartDeployDir", (node \ "remoteWebStartDeployDir").text)

			hashMap.asInstanceOf[HashMap[String,Any]]
	}	
	
	def get(param: String): String = {
		value(param).asInstanceOf[String]
	}
	
	def geti(param: String): Int = {
		value(param).asInstanceOf[Int]
	}
	
	def getb(param: String): Boolean = {
		value(param).asInstanceOf[Boolean]
	}
	
	def getlh(param: String): List[HashMap[String,String]] = {
		value(param).asInstanceOf[List[HashMap[String,String]]]
	}

	def getl(param: String): List[String] = {
		value(param).asInstanceOf[List[String]]
	}
	
	def loadPreferences: Preferences = {
		var sett = new Preferences(absolutePathToBotODB)
		var oldCfgFile = sett.get("configFile") // XXX: cause we don't write it's value to config. It's hardcoded
		try {
			val loadnode = XML.loadFile(absolutePathToBotODB + sett.get("configFile")) 
			sett.value = fromXML(loadnode)
			sett.value("configFile") = oldCfgFile
		} catch {
			case x: Throwable => {
				println("*** config file " +
						absolutePathToBotODB + sett.get("configFile") + " doesn't exists! creating new one")
				sett.savePreferences
			}
		}
		sett
	}

	def savePreferences = XML.saveFull( absolutePathToBotODB + get("configFile"), toXML, "UTF-8", true, null)
	
	def savePreferences(fileName: String) = XML.saveFull(fileName, toXML, "UTF-8", true, null)
	
}
