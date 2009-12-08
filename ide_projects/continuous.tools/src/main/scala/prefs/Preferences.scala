// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package prefs


import java.io.File
import org.apache.log4j.{Level, Logger}
import scala.collection.mutable.Map
import utils.Utils
import xml.{Node, XML}


class Preferences(configFileNameInput: String) extends Utils {

	def this() = this("project.tools.xml") // additional Constructor

	override def logger = Logger.getLogger(classOf[Preferences])
	initLogger
	val configFileName = System.getProperty("user.home") + "/" + ".codadris/" + configFileNameInput
	var value = Map(
		"debug" -> false,
		"xmppResourceString" -> "",
		"xmppLogin" -> "",
		"xmppPassword" -> "",
		"xmppServer" -> "",
		"xmppPort" -> 5222,
		"gitExecutable" -> "",
		"gitRepositoryProjectDir" -> "",
		"jarSignerPassword" -> "",
		"jarExecutable" -> "",
		"jarSignerExecutable" -> "",
		"jarSignerKeyName" -> "",
		"sshUserName" -> "",
		"sshPassword" -> "",
		"sshHost" -> "",
		"sshPort" -> 22,
		"users" -> List(
			Map( "user" -> "someone@somehost.domain", "params" -> "--numstat --no-merges" )
		),
		"deployFilesBasic" -> List(
			"some-project-specific.jar"
		),
		"deployFilesAdditionalDependencies" -> List(
			"some-project-specific-additional.jar"
		),
		"webstartArgumentsJVM" -> List(
		  	"-server"
		),
		"remoteWebStartDeployDir" -> "/home/your-user/deploydir",
		"deployOnlyBasicFiles" -> false,
		"projectGroupId" -> "/codadris",
		"remoteProjectToolsDir" -> "/home/yout-user/project-tools-dir-on-remote-shell-accoun",
		"remoteScalaBin" -> "/your/path/to/scala",
		"jnlpMainClass" -> "your.company.MainClass",
		"jnlpAppName" -> "Your Shiny Project name",
		"jnlpCodebase" -> "http://your.project.domain/",
		"jnlpHomePage" -> "http://your.company.homepage.domain/",
		"jnlpFileName" -> "launch.jnlp",
		"jnlpVendor" -> "Your Shiny Company",
		"jnlpIcon" -> "your_shiny_icon.png",
		"jnlpDescription" -> "Your shiny description",
		"directoryForLocalDeploy" -> ".your-company/dir/for/local/deploys",
		"xmppStatusDescription" -> "I am Your xmpp company bot! Here's my description",
		"xmppDatabaseFileName" -> "ScalaXMPPBotDataBase.neodatis",
		"databaseODBPort" -> 6660,
		"xmppDatabaseName" -> "xmppBotCommitDatabase",
		"xmppDatabaseListenAddress" -> "127.0.0.1",
		"ircDatabaseName" -> "ircBotDataBase",
		"ircDatabaseFileName" -> "ScalaIRCBotDataBase.neodatis",
		"ircDatabaseListenAddress" -> "127.0.0.1",
		"ircServer" -> "irc.freenode.net",
		"ircName" -> "shiny-bot",
		"ircDebugInfo" -> false,
		"ircAutoNickChange" -> true,
		"ircVersionString" -> "None v1.0",
		"ircEncoding" -> "UTF-8",
		"ircAutoJoinChannels" -> List(
			"#myshinychannel"
		)
	)


	def exc(x: Any, y: Any) = throw new RuntimeException("Error reading config file! Key: " + x + " has BAD value: \"" + y + "\"")

	def validateValues = {
		for (i <- value) i._2 match {
			case "" | Nil | null => exc(i._1, i._2)
			case list: List[Any] =>
				for (l <- list) l match {
					case Nil => exc(i, l)
					case _ =>
				}
			case _ =>
		}
	}

	def toXML =
		<preferences>
			<debug>{value("debug")}</debug>
			<gitExecutable>{value("gitExecutable")}</gitExecutable>
			<jarSignerPassword>{value("jarSignerPassword")}</jarSignerPassword>
			<jarExecutable>{value("jarExecutable")}</jarExecutable>
			<jarSignerExecutable>{value("jarSignerExecutable")}</jarSignerExecutable>
			<jarSignerKeyName>{value("jarSignerKeyName")}</jarSignerKeyName>
			<sshPassword>{value("sshPassword")}</sshPassword>
			<sshUserName>{value("sshUserName")}</sshUserName>
			<sshHost>{value("sshHost")}</sshHost>
			<sshPort>{value("sshPort")}</sshPort>
			<gitRepositoryProjectDir>{value("gitRepositoryProjectDir")}</gitRepositoryProjectDir>
			<xmppResourceString>{value("xmppResourceString")}</xmppResourceString>
			<xmppLogin>{value("xmppLogin")}</xmppLogin>
			<xmppPassword>{value("xmppPassword")}</xmppPassword>
			<xmppServer>{value("xmppServer")}</xmppServer>
			<xmppPort>{value("xmppPort")}</xmppPort>
			<xmppDatabaseFileName>{value("xmppDatabaseFileName")}</xmppDatabaseFileName>
			<xmppStatusDescription>{value("xmppStatusDescription")}</xmppStatusDescription>
			<databaseODBPort>{value("databaseODBPort")}</databaseODBPort>
			<xmppDatabaseName>{value("xmppDatabaseName")}</xmppDatabaseName>
			<xmppDatabaseListenAddress>{value("xmppDatabaseListenAddress")}</xmppDatabaseListenAddress>
			<projectGroupId>{value("projectGroupId")}</projectGroupId>
			<users>
			{
				for( i <- getlh("users"))
				yield
				<user>
					<name>{i("user")}</name>
					<params>{i("params")}</params>
				</user>
			}
			</users>
			<deployFilesBasic>
			{
				for( i <- getl("deployFilesBasic"))
				yield
				<file>
				{i}
				</file>
			}
			</deployFilesBasic>
			<deployFilesAdditionalDependencies>
			{
				for( i <- getl("deployFilesAdditionalDependencies"))
				yield
				<fileDep>
				{i}
				</fileDep>
			}
			</deployFilesAdditionalDependencies>
			<webstartArgumentsJVM>
			{
				for( i <- getl("webstartArgumentsJVM"))
				yield
				<arg>
				{i}
				</arg>
			}
			</webstartArgumentsJVM>
			<remoteWebStartDeployDir>{value("remoteWebStartDeployDir")}</remoteWebStartDeployDir>
			<deployOnlyBasicFiles>{value("deployOnlyBasicFiles")}</deployOnlyBasicFiles>
			<remoteProjectToolsDir>{value("remoteProjectToolsDir")}</remoteProjectToolsDir>
			<remoteScalaBin>{value("remoteScalaBin")}</remoteScalaBin>
			<jnlpMainClass>{value("jnlpMainClass")}</jnlpMainClass>
			<jnlpAppName>{value("jnlpAppName")}</jnlpAppName>
			<jnlpCodebase>{value("jnlpCodebase")}</jnlpCodebase>
			<jnlpFileName>{value("jnlpFileName")}</jnlpFileName>
			<jnlpVendor>{value("jnlpVendor")}</jnlpVendor>
			<jnlpHomePage>{value("jnlpHomePage")}</jnlpHomePage>
			<jnlpIcon>{value("jnlpIcon")}</jnlpIcon>
			<jnlpDescription>{value("jnlpDescription")}</jnlpDescription>
			<directoryForLocalDeploy>{value("directoryForLocalDeploy")}</directoryForLocalDeploy>
			<ircDatabaseName>{value("ircDatabaseName")}</ircDatabaseName>
			<ircDatabaseFileName>{value("ircDatabaseFileName")}</ircDatabaseFileName>
			<ircDatabaseListenAddress>{value("ircDatabaseListenAddress")}</ircDatabaseListenAddress>
			<ircServer>{value("ircServer")}</ircServer>
			<ircName>{value("ircName")}</ircName>
			<ircDebugInfo>{value("ircDebugInfo")}</ircDebugInfo>
			<ircAutoNickChange>{value("ircAutoNickChange")}</ircAutoNickChange>
			<ircVersionString>{value("ircVersionString")}</ircVersionString>
			<ircEncoding>{value("ircEncoding")}</ircEncoding>
			<ircAutoJoinChannels>
			{
				for( i <- getl("ircAutoJoinChannels"))
				yield
				<channel>
				{i}
				</channel>
			}
			</ircAutoJoinChannels>
		</preferences>


	def fromXML(node: Node) = {
		val map = Map[String,Any]()
		try {
			map.update( "debug", (node \ "debug").text.toBoolean)
			map.update( "gitExecutable", (node \ "gitExecutable").text.trim)
			map.update( "jarSignerPassword", (node \ "jarSignerPassword").text.trim)
			map.update( "jarExecutable", (node \ "jarExecutable").text.trim)
			map.update( "jarSignerExecutable", (node \ "jarSignerExecutable").text.trim)
			map.update( "jarSignerKeyName", (node \ "jarSignerKeyName").text.trim)
			map.update( "sshPassword", (node \ "sshPassword").text.trim)
			map.update( "sshUserName", (node \ "sshUserName").text.trim)
			map.update( "sshHost", (node \ "sshHost").text.trim)
			map.update( "sshPort", (node \ "sshPort").text.toInt)
			map.update( "gitRepositoryProjectDir", (node \ "gitRepositoryProjectDir").text.trim)
			map.update( "xmppResourceString", (node \ "xmppResourceString").text.trim)
			map.update( "xmppLogin", (node \ "xmppLogin").text.trim)
			map.update( "xmppPassword", (node \ "xmppPassword").text.trim)
			map.update( "xmppServer", (node \ "xmppServer").text.trim)
			map.update( "xmppPort", (node \ "xmppPort").text.toInt)
			map.update( "xmppDatabaseFileName", (node \ "xmppDatabaseFileName").text.trim)
			map.update( "xmppStatusDescription", (node \ "xmppStatusDescription").text.trim)
			map.update( "databaseODBPort", (node \ "databaseODBPort").text.toInt)
			map.update( "xmppDatabaseName", (node \ "xmppDatabaseName").text.trim)
			map.update( "xmppDatabaseListenAddress", (node \ "xmppDatabaseListenAddress").text.trim)

			var mapList = List[Map[String,String]]()
			(node \\ "user").foreach { user =>
				user.foreach { nod =>
					val name = (nod \ "name").text.trim
					val params = (nod \ "params").text.trim
					mapList ::= Map( "user" -> name, "params" -> params )
				}
			}
			map.update( "users", mapList )

			var list2 = List[String]()
			(node \\ "file").foreach { file =>
				list2 ::= file.text.trim
			}
			map.update( "deployFilesBasic", list2 )

			list2 = List[String]()
			(node \\ "fileDep").foreach { file =>
				list2 ::= file.text.trim
			}
			map.update( "deployFilesAdditionalDependencies", list2 )

			list2 = List[String]()
			(node \\ "arg").foreach { file =>
				list2 ::= file.text.trim
			}
			map.update( "webstartArgumentsJVM", list2 )

			map.update( "remoteWebStartDeployDir", (node \ "remoteWebStartDeployDir").text.trim)
		    map.update( "deployOnlyBasicFiles", (node \ "deployOnlyBasicFiles").text.toBoolean)
			map.update( "remoteProjectToolsDir", (node \ "remoteProjectToolsDir").text.trim)
			map.update( "projectGroupId", (node \ "projectGroupId").text.trim)
			map.update( "remoteScalaBin", (node \ "remoteScalaBin").text.trim)
			map.update( "jnlpMainClass", (node \ "jnlpMainClass").text.trim)
			map.update( "jnlpAppName", (node \ "jnlpAppName").text.trim)
			map.update( "jnlpCodebase", (node \ "jnlpCodebase").text.trim)
			map.update( "jnlpFileName", (node \ "jnlpFileName").text.trim)
			map.update( "jnlpVendor", (node \ "jnlpVendor").text.trim)
			map.update( "jnlpHomePage", (node \ "jnlpHomePage").text.trim)
			map.update( "jnlpIcon", (node \ "jnlpIcon").text.trim)
			map.update( "jnlpDescription", (node \ "jnlpDescription").text.trim)
			map.update( "directoryForLocalDeploy", (node \ "directoryForLocalDeploy").text.trim)
			map.update( "ircDatabaseName", (node \ "ircDatabaseName").text.trim)
			map.update( "ircDatabaseFileName", (node \ "ircDatabaseFileName").text.trim)
			map.update( "ircDatabaseListenAddress", (node \ "ircDatabaseListenAddress").text.trim)
			map.update( "ircServer", (node \ "ircServer").text.trim)
			map.update( "ircName", (node \ "ircName").text.trim)
			map.update( "ircDebugInfo", (node \ "ircDebugInfo").text.toBoolean)
			map.update( "ircAutoNickChange", (node \ "ircAutoNickChange").text.toBoolean)
			map.update( "ircVersionString", (node \ "ircVersionString").text.trim)
			map.update( "ircEncoding", (node \ "ircEncoding").text.trim)
		    list2 = List[String]()
			(node \\ "channel").foreach { file =>
				list2 ::= file.text.trim
			}
			map.update( "ircAutoJoinChannels", list2 )
			map
		} catch {
			case x: Exception => {
				logger.error("Exception while taking value from file!: " + x)
				map
			}
		}
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
	
	def getlh(param: String): List[Map[String,String]] = {
		value(param).asInstanceOf[List[Map[String,String]]]
	}

	def getl(param: String): List[String] = {
		value(param).asInstanceOf[List[String]]
	}
	
	def savePreferences = {
		autoDetectRequirements(this)
		XML.saveFull(configFileName, toXML, "UTF-8", true, null)
	}

	try { // read values from config or generate new stub
		value = fromXML(XML.loadFile(configFileName))
		validateValues
		logger.info("Config file used (" + configFileName + ")")
	} catch {
		case x: RuntimeException => {
			logger.error("Bad Value in config file found:\n " + x)
			System.exit(1)
		}
		case x: Exception => {
			logger.warn("*** Config file " + configFileName + " doesn't exists! Creating new one")
			savePreferences
		}
	}

}
