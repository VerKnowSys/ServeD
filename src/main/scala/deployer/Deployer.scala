// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package deployer


import actors._
import java.io.{OutputStreamWriter, File}
import java.util.{UUID, Date, ArrayList}
import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
import java.util.regex.{Matcher, Pattern}
import prefs.{PreferencesActor, Preferences}

/**
 * User: dmilith
 * Date: Jun 26, 2009
 * Time: 12:04:35 PM
 */

trait P {
	def accept(t: String): Boolean
}

object Deployer extends Actor {

	private val basicOnly_? = true
	private val uuid = UUID.randomUUID.toString
	private val filesToBeDeployed = new ArrayList[File]()
	private val pathToMaven2Repo = System.getProperty("user.home") + "/.m2/repository/"
	private val logger = Logger.getLogger(Deployer.getClass)
	private var prefs: Preferences = null
	private var debug: Boolean = false

	val BASIC_JAR_NAMES = Array[String](
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
	)

	val DEPENDENCY_JAR_NAMES = Array[String] (
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
    )

	def addShutdownHook =
		Runtime.getRuntime.addShutdownHook( new Thread {
			override def run = {
				PreferencesActor ! 'Quit
				Deployer ! 'End
				println("Done\n")
			}
		})

	def initLogger = {
		val appender = new ConsoleAppender
		appender.setName(ConsoleAppender.SYSTEM_OUT);
		appender.setWriter(new OutputStreamWriter(System.out))
		val level = Level.INFO
		appender.setThreshold(level)
		appender.setLayout(new PatternLayout("{ %-5p %d : %m }%n"));
		Logger.getRootLogger.addAppender(appender)
	}

	def findFile(f: File, p: P, r: ArrayList[File]) {
		if (f.isDirectory) {
			val files = f.listFiles
			for (i <- 0 until files.length) {
				findFile(files(i), p, r)
			}
		} else if (p.accept(f + "")) {
			r.add(f)
		}
	}

	def doDeploy = {
		logger.info("User home: " + System.getProperty("user.home"))
		logger.info("Path to repo: " + pathToMaven2Repo)

		var jar_names = Array[String]()
		if (basicOnly_?) jar_names = BASIC_JAR_NAMES else jar_names = BASIC_JAR_NAMES ++ DEPENDENCY_JAR_NAMES

		logger.info("Searching for jars in Maven repository.")
		jar_names foreach { file =>
				findFile(new File(pathToMaven2Repo), new P {
					override
					def accept(t: String): Boolean = {
						val fileRegex = ".*" + file + "$"
						val pattern = Pattern.compile(fileRegex)
						val mat = pattern.matcher(t)
						if ( mat.find ) {
							print(".")
							Deployer ! ('DoSign, t)
							return true
						}
						return false
					}
				}, filesToBeDeployed)
		}
		println
		logger.info("Done.\nFiles to be deployed:\n" + filesToBeDeployed.toArray.map{ a => "\n" + a.toString })
		Deployer ! 'End
	}

	def signJar(fileToBeSigned: String) = {
		logger.info("Preparing for signing jar: " + fileToBeSigned)
		val deployDir = "/tmp/deployer-" + uuid
		new File(deployDir).mkdir
		logger.info("Deploy tmp dir: " + deployDir)
		val command = Array("echo", prefs.get("jarSignerPassword") + "|", prefs.get("jarSignerExecutable"), fileToBeSigned, prefs.get("jarSignerKeyName"))
		logger.info("Will sign with command: " + command.map{ a => a })
	}

	override
	def act = {
		Actor.loop {
			react {
				case s: Preferences => {
					prefs = s
					debug = prefs.getb("debug")
					doDeploy
					act
				}
				case ('DoSign, whichOne: String) => {
					signJar(whichOne)
					act
				}
				case 'End => {
					PreferencesActor ! 'Quit
					exit
				}
			}
		}
	}

	def main(args: Array[String]) {
		val arguments = Array[String]("./") // XXX: should be based on given arguments
		initLogger
		addShutdownHook
		logger.info("Starting Deployer")
		this.start
		PreferencesActor.start
		PreferencesActor ! arguments
		PreferencesActor ! 'DeployerNeedPreferences // tell PreferencesActor that DeployerActor wants his settings
	}

}