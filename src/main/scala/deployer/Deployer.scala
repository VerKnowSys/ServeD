// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package deployer


import actors._
import signals.{Init, Quit}
import command.exec.CommandExec
import jar.comparator.JarEntryComparator
import java.io._
import java.util.{UUID, Date, ArrayList}
import org.apache.commons.io.{FileUtils, CopyUtils}
import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
import java.util.regex.{Matcher, Pattern}
import prefs.Preferences
import ssh.tools.SSHActor
import utils.Utils

/**
 * User: dmilith
 * Date: Jun 26, 2009
 * Time: 12:04:35 PM
 */


object Deployer extends Actor with Utils {

	private val filesToBeDeployed = new ArrayList[File]()
	private val uuid = UUID.randomUUID.toString
	private val deployTmpDir = "/tmp/deployer-" + uuid + "/"
	private val pathToMaven2Repo = System.getProperty("user.home") + "/.m2/repository/"
	var logger = Logger.getLogger(Deployer.getClass)
	initLogger
	private var prefs = new Preferences // default file name
	private val debug = prefs.getb("debug")
	if (debug) {
		setLoggerLevelDebug(Level.TRACE)
	}
	private var basicOnly_? = prefs.getb("deployOnlyBasicFiles")
	private val basic_jar_names = prefs.getl("deployFilesBasic")
	private val dependency_jar_names = prefs.getl("deployFilesAdditionalDependencies")
	private val codebaseLocalDir = System.getProperty("user.home") + "/" + prefs.get("directoryForLocalDeploy")
	private var trunk = false


	def getFilesFromMavenRepositoryAndSignThem = {
		logger.warn("Searching for jars in Maven repository and signing them..")
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
		logger.warn("Signing jar: " + fileToBeSigned.split("/").last)
		FileUtils.copyFileToDirectory(new File(fileToBeSigned), new File(deployTmpDir)) // copy files to temporary dir
		val signCommand = Array(
			prefs.get("jarSignerExecutable"), "-storepass", prefs.get("jarSignerPassword"),
			deployTmpDir + fileToBeSigned.split("/").last,	prefs.get("jarSignerKeyName")
			)
		logger.debug(CommandExec.cmdExec(signCommand).trim)
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
					logger.error("Unsupported action received!")
				}
			}
		}
	}


	def backupLocal = {
		val backupDate = (new Date).toString.replaceAll(" |:", "_")
		val backupDir = new File(codebaseLocalDir + "../OLD_DEPLOYS/OLD_LOCAL_DEPLOY_" +  backupDate + "/")
		val localDeployDir = new File (codebaseLocalDir)
		backupDir.mkdir
		localDeployDir.mkdir
		logger.warn("Copying files from " + localDeployDir + " to " + backupDir)
		FileUtils.copyDirectory(localDeployDir, backupDir)
	}


	def deployLocal = {
		backupLocal
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

		addShutdownHook {
			SSHActor ! Quit
			Deployer ! Quit
			logger.warn("Done.")
		}
		if (args.size == 0) {
			logger.error("Missing argument: (config filename)")
			exit
		}
		prefs = new Preferences(args(0))
		logger.warn("Starting Deployer..")
		logger.warn("User home dir: " + System.getProperty("user.home"))
		logger.warn("Maven 2 Repository dir: " + pathToMaven2Repo)
		logger.warn("Deploy tmp dir: " + deployTmpDir)
		logger.warn("Given arguments:")
		args.foreach{
			a => logger.warn(a)
		}

		// check given arguments
		if (args.size > 0) {
			for (arg <- args) {
				arg match {
					case "mac-app" => {
						logger.warn("Requested to perform Mac Application instead of web-start app\nNYI!")
						// TODO: implement Mac Application deploy support
					}
					case "local-full" => {
						// local full deploy without ssh actor
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
					case "trunk" => {
						// remote trunk deploy
						logger.warn("Requested to perform trunk deploy")
						this.start
						SSHActor.start
						SSHActor ! Init
						getFilesFromMavenRepositoryAndSignThem
						trunk = true
						SSHActor ! (filesToBeDeployed, uuid, deployTmpDir, trunk)
					}
					case "trunk-full" => {
						// remote full trunk deploy
						logger.warn("Requested to perform FULL trunk deploy")
						this.start
						SSHActor.start
						SSHActor ! Init
						basicOnly_? = false
						trunk = true
						getFilesFromMavenRepositoryAndSignThem
						SSHActor ! (filesToBeDeployed, uuid, deployTmpDir, trunk)
					}
					case "help" => {
						// help
						logger.warn("\n\nDeployer quick help:\nValid params:\n" +
								"\ttrunk -> for trunk deploy\n" +
								"\ttrunk-full -> for full trunk deploy\n" +
								"\tlocal -> for basic local deploy\n" +
								"\tlocal-full -> for full local deploy\n" +
								"\tmac-app -> for Macintosh application (NYI)\n" +
								"\tTo run remote deploying, no arguments (default).")
					}
					case _ => {

					}
				}
			}
		} else {
			logger.warn("Requested to perform standard remote deploy")
			this.start
			// normal deploy based on config values
			SSHActor.start
			SSHActor ! Init
			getFilesFromMavenRepositoryAndSignThem
			SSHActor ! (filesToBeDeployed, uuid, deployTmpDir, trunk)
		}
	}

}