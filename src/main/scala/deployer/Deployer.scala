// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package deployer


import actors._
import cases.{Init, Quit}

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
import prefs.Preferences
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

	private val filesToBeDeployed = new ArrayList[File]()
	private val uuid = UUID.randomUUID.toString
	private val deployTmpDir = "/tmp/deployer-" + uuid + "/"
	private val pathToMaven2Repo = System.getProperty("user.home") + "/.m2/repository/"
	private val logger = Logger.getLogger(Deployer.getClass)
	private val prefs: Preferences = (new Preferences).loadPreferences
	private val debug: Boolean = prefs.getb("debug")
	private var basicOnly_? = prefs.getb("deployOnlyBasicFiles")
	private val basic_jar_names = prefs.getl("deployFilesBasic")
	private val dependency_jar_names = prefs.getl("deployFilesAdditionalDependencies")

	def addShutdownHook =
		Runtime.getRuntime.addShutdownHook( new Thread {
			override def run = {
				SSHActor ! Quit
				Deployer ! Quit
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
	}

	def signJar(fileToBeSigned: String) = {
		new File(deployTmpDir).mkdir // make temporary place for jars before signinig
		logger.info("Preparing for signing jar: " + deployTmpDir + fileToBeSigned.split("/").last)
		FileUtils.copyFileToDirectory(new File(fileToBeSigned), new File(deployTmpDir)) // copy files to temporary dir
		val signCommand = Array(
			prefs.get("jarSignerExecutable"), "-storepass", prefs.get("jarSignerPassword"),
			deployTmpDir + fileToBeSigned.split("/").last,	prefs.get("jarSignerKeyName")
			)
		logger.info(CommandExec.cmdExec(signCommand).trim)
	}

	override
	def act = {
		Actor.loop {
			react {
				case Quit => {
					SSHActor ! Quit
					exit
				}
				case _ => {
					logger.warn("Unsupported action received")
				}
			}
		}
	}

	
	def deployLocal = {

		val codebaseLocalDir: String = System.getProperty("user.home") + "/LOCAL_COVIOB2_DEPLOY/"
		filesToBeDeployed.toArray foreach {
			localFile => {
				val comparator = new JarEntryComparator
				comparator.load(deployTmpDir + localFile.toString.split("/").last, codebaseLocalDir + "lib/" + localFile.toString.split("/").last)
				if (comparator.diff_?) {
					logger.warn("File DIFFERENT: " + localFile.toString.split("/").last)
					FileUtils.copyFileToDirectory( new File(deployTmpDir + localFile.toString.split("/").last), new File(codebaseLocalDir + "lib/"))
				} else {
					logger.warn("File IDENTICAL: " + localFile.toString.split("/").last)
				}
			}
		}
		logger.warn("Generating JNLP file")
		var arguments = ""
		for( i <- prefs.getl("webstartArgumentsJVM")) {
			arguments += i + " "
		}
		val jnlp = new JNLPSkeleton(
			prefs.get("jnlpMainClass"),
			prefs.get("jnlpAppName"),
			"file://" + codebaseLocalDir,
			prefs.get("jnlpFileName"),
			(prefs.getl("deployFilesBasic") ++ prefs.getl("deployFilesAdditionalDependencies")),
			arguments,
			prefs.get("jnlpVendor"),
			"file://" + codebaseLocalDir,
			prefs.get("jnlpIcon"),
			prefs.get("jnlpDescription")
			)
		val tempJnlpFileName = "/tmp/" + prefs.get("jnlpFileName") 
		jnlp.saveJNLP(tempJnlpFileName)
		logger.warn("Putting JNLP file to local deploy dir")
		FileUtils.copyFileToDirectory(new File(tempJnlpFileName), new File(codebaseLocalDir))
	}

	
	def main(args: Array[String]) {
		addShutdownHook
		initLogger
		logger.info("User home dir: " + System.getProperty("user.home"))
		logger.info("Working dir: " + System.getProperty("user.dir"))
		logger.info("Maven 2 Repository dir: " + pathToMaven2Repo)
		logger.info("Deploy tmp dir: " + deployTmpDir)
		logger.warn("Starting Deployer..")

		// check given arguments
		if (args.size > 0) {

			for (arg <- args) {
				arg match {
					case "mac-app" => {
						logger.warn("Requested to perform Mac Application instead of web-start app\nNYI!")
						// TODO: implement Mac Application deploy support
					}
					case "full-local" => {
						// local deploy without ssh actor
						logger.warn("Requested to perform FULL local deploy")
						basicOnly_? = false
						getFilesFromMavenRepositoryAndSignThem
						deployLocal
					}
					case "local" => {
						// local deploy without ssh actor
						logger.warn("Requested to perform local deploy")
						getFilesFromMavenRepositoryAndSignThem
						deployLocal
					}
					case _ => {
						logger.warn("Bad parameters. Valid params are:\nlocal - for basic local deploy\n" +
								"full-local - for full local deploy\n" +
								"mac-app - for Macintosh application\n" +
								"none for remote ssh deploy based on project.tools.xml config file.")
					}
				}
			}
			
		} else {

			this.start
			// normal deploy based on config values
			SSHActor.start
			SSHActor ! Init
			getFilesFromMavenRepositoryAndSignThem
			SSHActor ! (filesToBeDeployed, uuid, deployTmpDir)
		}
	}

}