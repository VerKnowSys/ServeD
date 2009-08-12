// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package prefs


import java.io.File
import java.util.ArrayList
import java.util.regex.Pattern
import org.apache.log4j.Logger
import scala.collection.mutable.HashMap
import scala.xml.XML
import utils.Utils


class Preferences(configFileNameInput: String) extends Utils {

	def this() = this("project.tools.xml") // additional Constructor
	override
	def logger = Logger.getLogger(classOf[Preferences])
	val configFileName = System.getProperty("user.home") + "/" + ".codadris/" + configFileNameInput
	var value = HashMap[String,Any] ( // XXX hardcoded values will be removed when GUI will be ready
		"debug" -> false,
		"xmppResourceString" -> "scalaBot-2",
		"xmppLogin" -> "varra",
		"xmppPassword" -> "varrajabber",
		"xmppServer" -> "drakor.eu",
		"xmppPort" -> 5222,
		"gitExecutable" -> "git",
		"gitRepositoryProjectDir" -> "/git/scala.project.tools.git/.git",
		"jarSignerPassword" -> "gru5zka.",
		"jarSignerExecutable" -> "jarsigner",
		"jarSignerKeyName" -> "VerKnowSys",
		"sshUserName" -> "verknowsys",
		"sshPassword" -> "gru5zka.",
		"sshHost" -> "drakor.eu",
		"sshPort" -> 22,
		"users" -> List(
			HashMap( "user" -> "dmilith@jabber.verknowsys.info", "params" -> "--numstat --no-merges --abbrev-commit" ),
			HashMap( "user" -> "szymon@jez.net.pl", "params" -> "--full-diff --numstat --no-merges" ),
			HashMap( "user" -> "karolrvn@jabber.verknowsys.info", "params" -> "--numstat --no-merges" ),
			HashMap( "user" -> "vara@jabber.verknowsys.info", "params" -> "--numstat --no-merges" )
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
			"scala-library-2.7.5.jar",
			"org.quaqua.swing-layout-5.4.1.jar",
			"org.quaqua-5.4.1.jar",
			"substance-5.1.jar",
			"beansbinding-1.2.1.jar"
		),
		"webstartArgumentsJVM" -> List(
		  	"-Dcom.apple.macos.useScreenMenuBar=true",
			"-Dcom.apple.mrj.application.apple.menu.about.name=Coviob2",
			"-Dcom.apple.mrj.application.growbox.intrudes=false",
            "-server",
            "-Xmx1024m",
            "-XX:MaxPermSize=512m",
			"-XX:+UseCompressedOops",
			"-XX:MaxHeapFreeRatio=10",
			"-XX:MinHeapFreeRatio=5",
			"-XX:+AggressiveOpts",
			"-XX:CompileThreshold=1000",
			"-ea",
			"-XX:+UseParallelGC"
		),
		"remoteWebStartDeployDir" -> "/home/verknowsys/public_html/javaws/coviob2/trunk/",
		"deployOnlyBasicFiles" -> true,
		"remoteProjectToolsDir" -> "/home/verknowsys/JAVA/project.tools/",
		"remoteScalaBin" -> "/home/verknowsys/JAVA/scala/bin/scala",
		"jnlpMainClass" -> "codadris.coviob2.App_Coviob2",
		"jnlpAppName" -> "Coviob 2",
		"jnlpCodebase" -> "http://trunk.verknowsys.com/",
		"jnlpHomePage" -> "http://trunk.verknowsys.com/",
		"jnlpFileName" -> "launch.jnlp",
		"jnlpVendor" -> "VerKnowSys",
		"jnlpIcon" -> "LogoIcon_Coviob2.svg.png",
		"jnlpDescription" -> "COmunicate VIa OBjects",
		"directoryForLocalDeploy" -> ".codadris/LOCAL_COVIOB2_DEPLOY_DIR/",
		"xmppStatusDescription" -> "I should work fine.",
		"xmppDatabaseFileName" -> "ScalaXMPPBotDataBase.neodatis",
		"databaseODBPort" -> 50609,
		"xmppDatabaseName" -> "xmppBotCommitDatabase",
		"xmppDatabaseListenAddress" -> "127.0.0.1",
		"ircDatabaseName" -> "ircBotDataBase",
		"ircDatabaseFileName" -> "ScalaIRCBotDataBase.neodatis",
		"ircDatabaseListenAddress" -> "127.0.0.1",
		"ircServer" -> "irc.freenode.net",
		"ircName" -> "linkB",
		"ircDebugInfo" -> false,
		"ircAutoNickChange" -> true,
		"ircVersionString" -> "None v1.0",
		"ircEncoding" -> "UTF-8",
		"ircAutoJoinChannels" -> List(
			"#scala.pl",
			"#scala",
			"#ruby.pl"
		)
	)
	
	def toXML = 
		<preferences> 
			<debug>{value("debug")}</debug> 
			<gitExecutable>{value("gitExecutable")}</gitExecutable>
			<jarSignerPassword>{value("jarSignerPassword")}</jarSignerPassword>
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
				<fileDep>
				{i}
				</fileDep>
			}
			</deployFilesAdditionalDependencies>
			<webstartArgumentsJVM>
			{
				val list = value("webstartArgumentsJVM").asInstanceOf[List[String]]
				for( i <- list)
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
				val list = value("ircAutoJoinChannels").asInstanceOf[List[String]]
				for( i <- list)
				yield
				<channel>
				{i}
				</channel>
			}
			</ircAutoJoinChannels>
		</preferences>
	
	def fromXML(node: scala.xml.Node): HashMap[String,Any] = {
		val hashMap = HashMap[String,Any]()
			hashMap.update( "debug", (node \ "debug").text.toBoolean)
			hashMap.update( "gitExecutable", (node \ "gitExecutable").text.trim)
			hashMap.update( "jarSignerPassword", (node \ "jarSignerPassword").text.trim)
			hashMap.update( "jarSignerExecutable", (node \ "jarSignerExecutable").text.trim)
			hashMap.update( "jarSignerKeyName", (node \ "jarSignerKeyName").text.trim)
			hashMap.update( "sshPassword", (node \ "sshPassword").text.trim)
			hashMap.update( "sshUserName", (node \ "sshUserName").text.trim)
			hashMap.update( "sshHost", (node \ "sshHost").text.trim)
			hashMap.update( "sshPort", (node \ "sshPort").text.toInt)
			hashMap.update( "gitRepositoryProjectDir", (node \ "gitRepositoryProjectDir").text.trim)
			hashMap.update( "xmppResourceString", (node \ "xmppResourceString").text.trim)
			hashMap.update( "xmppLogin", (node \ "xmppLogin").text.trim)
			hashMap.update( "xmppPassword", (node \ "xmppPassword").text.trim)
			hashMap.update( "xmppServer", (node \ "xmppServer").text.trim)
			hashMap.update( "xmppPort", (node \ "xmppPort").text.toInt)
			hashMap.update( "xmppDatabaseFileName", (node \ "xmppDatabaseFileName").text.trim)
			hashMap.update( "xmppStatusDescription", (node \ "xmppStatusDescription").text.trim)
			hashMap.update( "databaseODBPort", (node \ "databaseODBPort").text.toInt)
			hashMap.update( "xmppDatabaseName", (node \ "xmppDatabaseName").text.trim)
			hashMap.update( "xmppDatabaseListenAddress", (node \ "xmppDatabaseListenAddress").text.trim)

			var hashMapList = List[HashMap[String,String]]()
			(node \\ "user").foreach { user =>
				user.foreach { nod =>
					val name = (nod \ "name").text.trim
					val params = (nod \ "params").text.trim
					hashMapList = hashMapList ::: List( HashMap( "user" -> name, "params" -> params ) ).asInstanceOf[List[HashMap[String,String]]]
				}
			}
			hashMap.update( "users", hashMapList )

			var list2 = List[String]()
			(node \\ "file").foreach { file =>
				list2 = list2 ::: List( file.text.trim ).asInstanceOf[List[String]]
			}
			hashMap.update( "deployFilesBasic", list2 )

			list2 = List[String]()
			(node \\ "fileDep").foreach { file =>
				list2 = list2 ::: List( file.text.trim ).asInstanceOf[List[String]]
			}
			hashMap.update( "deployFilesAdditionalDependencies", list2 )

			list2 = List[String]()
			(node \\ "arg").foreach { file =>
				list2 = list2 ::: List( file.text.trim ).asInstanceOf[List[String]]
			}
			hashMap.update( "webstartArgumentsJVM", list2 )

			hashMap.update( "remoteWebStartDeployDir", (node \ "remoteWebStartDeployDir").text.trim)
		    hashMap.update( "deployOnlyBasicFiles", (node \ "deployOnlyBasicFiles").text.toBoolean)
			hashMap.update( "remoteProjectToolsDir", (node \ "remoteProjectToolsDir").text.trim)
			hashMap.update( "remoteScalaBin", (node \ "remoteScalaBin").text.trim)
			hashMap.update( "jnlpMainClass", (node \ "jnlpMainClass").text.trim)
			hashMap.update( "jnlpAppName", (node \ "jnlpAppName").text.trim)
			hashMap.update( "jnlpCodebase", (node \ "jnlpCodebase").text.trim)
			hashMap.update( "jnlpFileName", (node \ "jnlpFileName").text.trim)
			hashMap.update( "jnlpVendor", (node \ "jnlpVendor").text.trim)
			hashMap.update( "jnlpHomePage", (node \ "jnlpHomePage").text.trim)
			hashMap.update( "jnlpIcon", (node \ "jnlpIcon").text.trim)
			hashMap.update( "jnlpDescription", (node \ "jnlpDescription").text.trim)
			hashMap.update( "directoryForLocalDeploy", (node \ "directoryForLocalDeploy").text.trim)
			hashMap.update( "ircDatabaseName", (node \ "ircDatabaseName").text.trim)
			hashMap.update( "ircDatabaseFileName", (node \ "ircDatabaseFileName").text.trim)
			hashMap.update( "ircDatabaseListenAddress", (node \ "ircDatabaseListenAddress").text.trim)
			hashMap.update( "ircServer", (node \ "ircServer").text.trim)
			hashMap.update( "ircName", (node \ "ircName").text.trim)
			hashMap.update( "ircDebugInfo", (node \ "ircDebugInfo").text.toBoolean)
			hashMap.update( "ircAutoNickChange", (node \ "ircAutoNickChange").text.toBoolean)
			hashMap.update( "ircVersionString", (node \ "ircVersionString").text.trim)
			hashMap.update( "ircEncoding", (node \ "ircEncoding").text.trim)
		    list2 = List[String]()
			(node \\ "channel").foreach { file =>
				list2 = list2 ::: List( file.text.trim ).asInstanceOf[List[String]]
			}
			hashMap.update( "ircAutoJoinChannels", list2 )
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
		try {
			value = fromXML(XML.loadFile(configFileName))
			logger.debug("*** Config file found (" + configFileName + ")")
		} catch {
			case x: Throwable => {
				logger.debug("*** Config file " + configFileName + " doesn't exists! Creating new one")
				savePreferences
			}
		}
		this
	}

	def savePreferences = {
		autoDetectRequirements(this)
		XML.saveFull(configFileName, toXML, "UTF-8", true, null)
	}

	loadPreferences
	
//	def savePreferences(fileName: String) = XML.saveFull(fileName, toXML, "UTF-8", true, null)
	
}
