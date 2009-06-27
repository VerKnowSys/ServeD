// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package deployer


import actors._
import collection.mutable.HashMap
import com.sshtools.j2ssh.{SshClient, SftpClient}
import com.sshtools.j2ssh.authentication.{PasswordAuthenticationClient, AuthenticationProtocolState}
import command.exec.CommandExec
import jar.comparator.JarEntryComparator
import java.io._
import java.util.{UUID, Date, ArrayList}
import org.apache.commons.io.{FileUtils, CopyUtils}
import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
import java.util.regex.{Matcher, Pattern}
import prefs.{PreferencesActor, Preferences}
import ssh.tools.SSHActor

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
	private val deployDir = "/tmp/deployer-" + uuid + "/"
	private val filesToBeDeployed = new ArrayList[File]()
	private val pathToMaven2Repo = System.getProperty("user.home") + "/.m2/repository/"
	private val logger = Logger.getLogger(Deployer.getClass)
	private var prefs: Preferences = null
	private var debug: Boolean = false

	var BASIC_JAR_NAMES: List[String] = _

	var DEPENDENCY_JAR_NAMES: List[String] = _

	def addShutdownHook =
		Runtime.getRuntime.addShutdownHook( new Thread {
			override def run = {
				PreferencesActor ! 'Quit
				SSHActor ! 'Quit
				Deployer ! 'Quit
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

	def getFilesFromMavenRepositoryAndSignThem = {
		var jar_names = List[String]()
		if (basicOnly_?) jar_names = BASIC_JAR_NAMES else jar_names = BASIC_JAR_NAMES ++ DEPENDENCY_JAR_NAMES

		logger.info("Searching for jars in Maven repository and signing them..")
		jar_names.foreach { file =>
			logger.info("*" + file.trim + "*")
			findFile(new File(pathToMaven2Repo), new P {
				override
				def accept(t: String): Boolean = {
					val fileRegex = ".*" + file.trim + "$"
					val pattern = Pattern.compile(fileRegex)
					val mat = pattern.matcher(t)
					if ( mat.find ) {
						signJar(t)
						return true
					}
					return false
				}
			}, filesToBeDeployed)
		}

//		println
//		logger.info("Done searching. Signed files:\n" + filesToBeDeployed.toArray.map{ a => "\n" + a.toString })
		
		logger.info("Preparing for deploying files to server  " + filesToBeDeployed.toArray )
		SSHActor ! ('PerformTasks, filesToBeDeployed, uuid)
	}

	def signJar(fileToBeSigned: String) = {
		logger.info("Preparing for signing jar: " + fileToBeSigned)
		new File(deployDir).mkdir // make temporary place for jars before signinig
		FileUtils.copyFileToDirectory(new File(fileToBeSigned), new File(deployDir)) // copy files to temporary dir
		val signCommand = Array(
			prefs.get("jarSignerExecutable"), "-storepass", prefs.get("jarSignerPassword"),
			deployDir + fileToBeSigned.split("/").last,	prefs.get("jarSignerKeyName")
			)
		println(CommandExec.cmdExec(signCommand).trim)
	}

	override
	def act = {
		Actor.loop {
			react {
				case s: Preferences => {
					prefs = s
					debug = prefs.getb("debug")
					BASIC_JAR_NAMES = prefs.getl("deployFilesBasic")
					DEPENDENCY_JAR_NAMES = prefs.getl("deployFilesAdditionalDependencies")
					getFilesFromMavenRepositoryAndSignThem
					act
				}
				case 'Quit => {
					PreferencesActor ! 'Quit
					SSHActor ! 'Quit
					exit
				}
			}
		}
	}

	def main(args: Array[String]) {
		val arguments = Array[String]("./") // XXX: should be based on given arguments
		initLogger
		addShutdownHook
		logger.info("User home: " + System.getProperty("user.home"))
		logger.info("Path to repo: " + pathToMaven2Repo)
		logger.info("Deploy tmp dir: " + deployDir)
		logger.info("Starting Deployer..")
		this.start
		SSHActor.start
		PreferencesActor.start
		PreferencesActor ! arguments
		PreferencesActor ! 'DeployerNeedPreferences // tell PreferencesActor that DeployerActor wants his settings
		PreferencesActor ! 'SSHActorNeedPreferences
	}

}