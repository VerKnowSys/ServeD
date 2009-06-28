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

	private val uuid = UUID.randomUUID.toString
	private val deployTmpDir = "/tmp/deployer-" + uuid + "/"
	private val filesToBeDeployed = new ArrayList[File]()
	private val pathToMaven2Repo = System.getProperty("user.home") + "/.m2/repository/"
	private val logger = Logger.getLogger(Deployer.getClass)
	private var prefs: Preferences = null
	private var debug: Boolean = false
	private var basicOnly_? = true
	private var basic_jar_names = List[String]()
	private var dependency_jar_names = List[String]()

	def addShutdownHook =
		Runtime.getRuntime.addShutdownHook( new Thread {
			override def run = {
				PreferencesActor ! 'Quit
				SSHActor ! 'Quit
				Deployer ! 'Quit
				logger.warn("Done\n")
			}
		})

	def initLogger = {
		val appender = new ConsoleAppender
		appender.setName(ConsoleAppender.SYSTEM_OUT);
		appender.setWriter(new OutputStreamWriter(System.out))
		val level = if (debug) Level.TRACE else Level.WARN
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
		logger.info("Searching for jars in Maven repository and signing them..")

		var jar_names = List[String]()
		if (basicOnly_?) {
			jar_names ++= basic_jar_names
			logger.warn("Selected only basic jars to deploy")
		} else {
			jar_names ++= basic_jar_names ++ dependency_jar_names
			logger.warn("Selected basic and dependendant jars to deploy")
		}
		jar_names.foreach { file =>
			logger.info("*" + file + "*")
			findFile(new File(pathToMaven2Repo), new P {
				override
				def accept(t: String): Boolean = {
					val fileRegex = ".*" + file + "$"
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

		SSHActor ! ('PerformTasks, filesToBeDeployed, uuid, deployTmpDir)
	}

	def signJar(fileToBeSigned: String) = {
		new File(deployTmpDir).mkdir // make temporary place for jars before signinig
		logger.info("Preparing for signing jar: " + deployTmpDir + fileToBeSigned.split("/").last)
		FileUtils.copyFileToDirectory(new File(fileToBeSigned), new File(deployTmpDir)) // copy files to temporary dir
		val signCommand = Array(
			prefs.get("jarSignerExecutable"), "-storepass", prefs.get("jarSignerPassword"),
			deployTmpDir + fileToBeSigned.split("/").last,	prefs.get("jarSignerKeyName")
			)
		logger.warn(CommandExec.cmdExec(signCommand).trim)
	}

	override
	def act = {
		Actor.loop {
			react {
				case s: Preferences => {
					prefs = s
					debug = prefs.getb("debug")
					basicOnly_? = prefs.getb("deployOnlyBasicFiles")
					basic_jar_names = prefs.getl("deployFilesBasic")
					dependency_jar_names = prefs.getl("deployFilesAdditionalDependencies")
					initLogger
					logger.info("User home: " + System.getProperty("user.home"))
					logger.info("Path to Maven2 repo: " + pathToMaven2Repo)
					logger.info("Deploy tmp dir: " + deployTmpDir)
					logger.warn("Starting Deployer..")
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
		addShutdownHook
		this.start
		SSHActor.start
		PreferencesActor.start
		PreferencesActor ! arguments
		PreferencesActor ! 'DeployerNeedPreferences // tell PreferencesActor that DeployerActor wants his settings
		PreferencesActor ! 'SSHActorNeedPreferences
	}

}